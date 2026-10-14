package Tactic.Squads

import Information.Battles.Battles
import Information.Battles.Types.Division
import Information.Geography.Geography
import Information.Geography.Pathfinding.Paths
import Information.Geography.Types.{Base, Zone}
import Information.Grids.Grids
import Information.Grids.Movement.{GridUnwalkableUnits, GridWalkable, GridWalkableTerrain}
import Lifecycle.With
import Mathematics.Points.{Pixel, Tile}
import Micro.Agency.{DestinationStack, Intention}
import Micro.Matchups.MatchupAnalysis
import Planning.Blackboard
import Planning.ResourceLocks.LockUnits
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.ProxyBWAPI
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitClasses.{UnitClass, UnitTypes}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import ProxyBwapi.UnitTracking.UnitTracker
import Utilities.Property
import Utilities.UnitCounters.CountEverything

import scala.collection.mutable.ArrayBuffer

object DarkTemplarSquadTest extends App {
  val unsafeField = classOf[sun.misc.Unsafe].getDeclaredField("theUnsafe")
  unsafeField.setAccessible(true)
  val unsafe = unsafeField.get(null).asInstanceOf[sun.misc.Unsafe]
  def allocate[T](clazz: Class[T]): T = unsafe.allocateInstance(clazz).asInstanceOf[T]
  def set(obj: AnyRef, name: String, value: Any): Unit = {
    val fields = Iterator.iterate[Class[_]](obj.getClass)(_.getSuperclass).takeWhile(_ != null)
      .flatMap(_.getDeclaredFields).filter(f => f.getName == name || f.getName.endsWith("$$" + name)).toVector
    assert(fields.nonEmpty, name)
    fields.foreach(f => { f.setAccessible(true); f.set(obj, value.asInstanceOf[AnyRef]) })
  }
  var passed = 0
  def check(name: String)(body: => Unit): Unit = { body; passed += 1; println("PASS " + name) }

  With.frame = 100
  With.mapTileWidth = 64; With.mapTileHeight = 64; With.mapTileArea = 4096
  With.mapPixelWidth = 2048; With.mapPixelHeight = 2048
  With.proxy = allocate(classOf[ProxyBWAPI])
  set(With.proxy, "unitClasses", UnitTypes.all.map(UnitClass))
  With.units = new UnitTracker
  With.battles = allocate(classOf[Battles])
  With.blackboard = allocate(classOf[Blackboard])
  set(With.blackboard, "darkTemplarSquad", new Property(true))
  With.geography = allocate(classOf[Geography])
  set(With.geography, "_bases", Vector.empty[Base])
  set(With.geography, "_zoneByTile", Array.fill[Zone](4096)(null))
  set(With.geography, "_baseByTile", Array.fill[Option[Base]](4096)(None))
  With.paths = allocate(classOf[Paths])
  With.grids = allocate(classOf[Grids])
  set(With.grids, "walkable", new GridWalkable)
  val terrain = new GridWalkableTerrain
  terrain.indices.foreach(terrain.set(_, true))
  set(With.grids, "walkableTerrain", terrain)
  set(With.grids, "unwalkableUnits", new GridUnwalkableUnits)

  def player(enemy: Boolean): PlayerInfo = new PlayerInfo(null) {
    override lazy val isUs = false
    override lazy val isEnemy = enemy
    override lazy val isNeutral = !enemy
    override def isProtoss = false
    override def isZerg = false
  }
  With.neutral = player(false)
  With.enemies = Vector(player(true))

  var nextId = 0
  def unit(kind: UnitClass, at: Pixel = Pixel(100, 100)): FriendlyUnitInfo = {
    val u = allocate(classOf[FriendlyUnitInfo])
    nextId += 1
    set(u, "id", nextId); set(u, "hashCode", nextId)
    set(u, "_unitClass", kind); set(u, "_pixel", at); set(u, "_tile", at.tile); set(u, "_flying", kind.isFlyer)
    set(u, "_orderTarget", None); set(u, "_orderTargetPixel", None)
    set(u, "_squad", None); set(u, "_intent", new Intention)
    u.matchups = new MatchupAnalysis(u) {
      override def enemyDetectorDeepest: Option[UnitInfo] = None
      override def threatDeepest: Option[UnitInfo] = None
    }
    u
  }
  def base(enemy: Boolean = false): Base = {
    val b = new Base("Test base", Tile(50, 50), Set.empty)
    b.owner = if (enemy) With.enemy else With.neutral
    b
  }
  // Keep real eligibility, ground-distance selection, and squad intentions; supply fixed group geometry.
  def division(at: Pixel, enemies: UnitInfo*): Division = new Division(enemies, Set.empty) {
    override def centroidGround: Pixel = at
  }
  class TestLock(s: SquadDarkTemplar) extends LockUnits(s, s.lock.matcher, s.lock.counter) {
    var acquired = false
    var released = false
    override def acquire(): collection.Set[FriendlyUnitInfo] = { acquired = true; s.units.toSet }
    override def release(): LockUnits = { released = true; this }
  }
  def squad(dts: Seq[FriendlyUnitInfo], bases: Base*): (SquadDarkTemplar, TestLock) = {
    val s = new SquadDarkTemplar
    assert(s.lock.matcher == Protoss.DarkTemplar && s.lock.counter == CountEverything)
    val lock = new TestLock(s)
    set(s, "lock", lock)
    set(s, "_unitsNow", ArrayBuffer(dts: _*))
    set(s, "bases", ArrayBuffer(bases: _*))
    dts.foreach(u => set(u, "_squad", Some(s)))
    (s, lock)
  }
  def nextDestination(u: FriendlyUnitInfo): Pixel = {
    val destinations = new DestinationStack {
      override lazy val unit: FriendlyUnitInfo = u
      override def defaultHome: Pixel = Pixel(32, 32)
    }
    destinations.resetDestinations()
    destinations.destinationNext()
  }
  def attacksDivision(dt: FriendlyUnitInfo, d: Division): Unit = {
    assert(dt.intent.targets.exists(_.toSet == d.enemies.toSet))
    assert(dt.intent.terminus.contains(d.centroidGround))
    assert(nextDestination(dt) == d.centroidGround)
    assert(dt.intent.canFight && dt.intent.canFlee)
  }

  check("vulnerable armies replace empty-base assignments") {
    val dt = unit(Protoss.DarkTemplar)
    val d = division(Pixel(300, 300), unit(Protoss.Dragoon))
    With.battles.divisions = Vector(d)
    val (s, lock) = squad(Vector(dt), base())
    s.run()
    attacksDivision(dt, d)
    assert(s.vicinity == d.centroidGround && !lock.released)
  }
  check("without eligible bases the squad recruits DTs and each chooses its nearest division") {
    val dtNear = unit(Protoss.DarkTemplar)
    val dtFar = unit(Protoss.DarkTemplar, Pixel(1500, 1500))
    val near = division(Pixel(300, 300), unit(Protoss.Dragoon))
    val far = division(Pixel(1400, 1400), unit(Terran.Marine))
    With.battles.divisions = Vector(far, near)
    val (s, lock) = squad(Vector(dtNear, dtFar))
    s.launch()
    assert(lock.acquired)
    s.run()
    attacksDivision(dtNear, near)
    attacksDivision(dtFar, far)
    assert(s.vicinity == near.centroidGround && !lock.released)
  }
  check("air-only divisions and divisions containing a detector are skipped") {
    val dt = unit(Protoss.DarkTemplar)
    val vulnerable = division(Pixel(900, 900), unit(Protoss.Dragoon))
    With.battles.divisions = Vector(
      division(Pixel(110, 110), unit(Protoss.Corsair)),
      division(Pixel(120, 120), unit(Protoss.Dragoon), unit(Protoss.Observer)),
      vulnerable)
    val (s, _) = squad(Vector(dt), base())
    s.run()
    attacksDivision(dt, vulnerable)
  }
  check("division eligibility is refreshed when a detector arrives") {
    val dt = unit(Protoss.DarkTemplar)
    val d = division(Pixel(300, 300), unit(Protoss.Dragoon))
    With.battles.divisions = Vector(d)
    val (s, lock) = squad(Vector(dt))
    s.run()
    attacksDivision(dt, d)
    With.frame += 1
    With.battles.divisions = Vector(division(d.centroidGround, (d.enemies.toSeq :+ unit(Protoss.Observer)): _*))
    s.run()
    assert(lock.released)
  }
  check("no eligible base or division leaves recruitment to other squads") {
    for (divisions <- Vector(Vector.empty[Division], Vector(division(Pixel(300, 300), unit(Protoss.Corsair))))) {
      With.battles.divisions = divisions
      val (s, lock) = squad(Vector(unit(Protoss.DarkTemplar)))
      s.launch()
      assert(!lock.acquired)
      s.run()
      assert(lock.released)
    }
  }
  check("an enemy base raid retains precedence over a vulnerable division") {
    val dt = unit(Protoss.DarkTemplar)
    val b = base(enemy = true)
    With.battles.divisions = Vector(division(Pixel(300, 300), unit(Protoss.Dragoon)))
    val (s, _) = squad(Vector(dt), b)
    s.run()
    assert(dt.intent.terminus.contains(b.heart.center))
    assert(dt.intent.targets.exists(_.isEmpty))
  }
  check("without a vulnerable division the existing base fallback remains") {
    val dt = unit(Protoss.DarkTemplar)
    val b = base()
    With.battles.divisions = Vector.empty
    val (s, _) = squad(Vector(dt), b)
    s.run()
    assert(dt.intent.terminus.contains(b.heart.center))
  }
  check("a squad awaiting its first DT can run with only a vulnerable division") {
    With.battles.divisions = Vector(division(Pixel(300, 300), unit(Protoss.Dragoon)))
    val (s, lock) = squad(Vector.empty)
    s.run()
    assert(!lock.released)
  }
  println(s"$passed dark templar squad tests passed")
}
