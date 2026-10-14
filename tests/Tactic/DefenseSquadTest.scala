package Tactic.Squads

import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Agency.{DestinationStack, Intention}
import Micro.Formation.{Formation, FormationSimple, FormationStyleDisengage, FormationStyleGuard, FormationStyleMarch}
import Performance.Cache
import ProxyBwapi.ProxyBWAPI
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitClasses.{UnitClass, UnitTypes}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

import scala.collection.mutable.ArrayBuffer

// Keep the real allocation and formation logic; omit map-dependent pursuit geometry.
class ScourTestSquad extends SquadDefendBase(null) {
  override def intendScouring(unit: FriendlyUnitInfo, target: UnitInfo): Unit = {
    assert(unit.canAttack(target), s"${unit.unitClass} cannot attack ${target.unitClass}")
    unit.intend(this).setTargets(target).setTerminus(target.pixel)
  }
  override def homeConsensus: Pixel = Pixel(64, 64)
}

object DefenseSquadTest extends App {
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
  val guardWithoutTargets = classOf[SquadDefendBase].getDeclaredMethods.find(_.getName.endsWith("guardWithoutTargets")).get
  guardWithoutTargets.setAccessible(true)
  var passed = 0
  def check(name: String)(body: => Unit): Unit = { body; passed += 1; println("PASS " + name) }
  With.frame = 100
  With.proxy = allocate(classOf[ProxyBWAPI])
  set(With.proxy, "unitClasses", UnitTypes.all.map(UnitClass))
  var nextId = 0
  def unit(kind: UnitClass, value: Double = 1.0): FriendlyUnitInfo = {
    val u = allocate(classOf[FriendlyUnitInfo])
    nextId += 1
    set(u, "id", nextId); set(u, "hashCode", nextId)
    set(u, "_unitClass", kind)
    set(u, "_pixel", Pixel(600 + nextId, 600))
    set(u, "_flying", kind.isFlyer)
    set(u, "_squad", None)
    set(u, "_intent", new Intention)
    set(u, "canAttackCache", new Cache(() => kind.rawCanAttack))
    set(u, "canBeAttackedCache", new Cache(() => true))
    set(u, "canMoveCache", new Cache(() => true))
    set(u, "_effectivelyCloaked", new Cache(() => false))
    set(u, "attacksAgainstAirCache", new Cache(() => if (kind.attacksAir) 1 else 0))
    set(u, "attacksAgainstGroundCache", new Cache(() => if (kind.attacksGround) 1 else 0))
    set(u, "pixelRangeAirCache", new Cache(() => kind.pixelRangeAir))
    set(u, "pixelRangeGroundCache", new Cache(() => kind.pixelRangeGround))
    set(u, "topSpeedPossibleCache", new Cache(() => kind.topSpeed))
    set(u, "subjectiveValueCache", new Cache(() => value))
    u
  }
  val post = Pixel(100, 100)
  val pursuit = Pixel(900, 900)
  def squad(defenders: Seq[FriendlyUnitInfo], targets: UnitInfo*): ScourTestSquad = {
    val s = allocate(classOf[ScourTestSquad])
    set(s, "_unitsNow", ArrayBuffer(defenders: _*))
    s.formations = ArrayBuffer(
      FormationSimple(FormationStyleMarch, defenders.map(_ -> pursuit).toMap),
      FormationSimple(FormationStyleGuard, defenders.map(_ -> post).toMap))
    s.setTargets(targets)
    defenders.foreach(u => set(u, "_squad", Some(s)))
    s
  }
  def nextDestination(u: FriendlyUnitInfo): Pixel = {
    val destinations = new DestinationStack {
      override lazy val unit: FriendlyUnitInfo = u
      override def defaultHome: Pixel = Pixel(64, 64)
    }
    destinations.resetDestinations()
    destinations.destinationNext()
  }
  def staysAtPost(u: FriendlyUnitInfo, at: Pixel = post): Unit = {
    assert(u.intent.targets.exists(_.isEmpty))
    assert(u.intent.terminus.contains(at))
    assert(u.intent.redoubt.contains(at))
    assert(u.intent.canFight && u.intent.canFlee)
    assert(nextDestination(u) == at, s"Defender still moves to ${nextDestination(u)}")
  }
  check("an Overlord draws only the Dragoon; Zealots retain their defensive destination") {
    val zealots = Vector.fill(2)(unit(Protoss.Zealot))
    val dragoon = unit(Protoss.Dragoon)
    val overlord = unit(Zerg.Overlord)
    val s = squad(zealots :+ dragoon, overlord)
    val original = s.formations.head
    s.formations(0) = new Formation {
      override def style = original.style
      override def placements = original.placements
      override def flanking: Boolean = true
    }
    s.scour()
    assert(s.formations.head.flanking)
    assert(dragoon.intent.targets.exists(_.contains(overlord)))
    assert(dragoon.stationEngage.contains(pursuit))
    zealots.foreach(staysAtPost(_))
  }
  check("ground intruders draw Zealots, while air-only Corsairs keep guarding") {
    val zealot = unit(Protoss.Zealot)
    val corsair = unit(Protoss.Corsair)
    val zergling = unit(Zerg.Zergling)
    squad(Vector(corsair, zealot), zergling).scour()
    assert(zealot.intent.targets.exists(_.contains(zergling)))
    staysAtPost(corsair)
  }
  check("surplus units choose a compatible target even after every allocation pass is full") {
    val zealots = Vector.fill(3)(unit(Protoss.Zealot, 100.0))
    val corsairs = Vector.fill(3)(unit(Protoss.Corsair, 100.0))
    val overlord = unit(Zerg.Overlord)
    val zergling = unit(Zerg.Zergling)
    squad(zealots ++ corsairs, overlord, zergling).scour()
    zealots.foreach(u => {
      assert(u.intent.targets.exists(_.contains(zergling)))
      assert(nextDestination(u) == zergling.pixel, "Ground defenders must not follow the Overlord's formation")
    })
    corsairs.foreach(u => assert(u.intent.targets.exists(_.contains(overlord))))
  }
  check("allocation checks the actual target, including ground enemies under Dark Swarm") {
    val dragoon = unit(Protoss.Dragoon)
    val zealot = unit(Protoss.Zealot)
    val zergling = unit(Zerg.Zergling)
    set(zergling, "_underDarkSwarm", true)
    assert(!dragoon.canAttack(zergling) && zealot.canAttack(zergling))
    squad(Vector(dragoon, zealot), zergling).scour()
    staysAtPost(dragoon)
    assert(zealot.intent.targets.exists(_.contains(zergling)))
  }
  check("an unattackable target does not exhaust or loop the allocation pool") {
    val zealot = unit(Protoss.Zealot)
    val darkTemplar = unit(Protoss.DarkTemplar)
    set(darkTemplar, "_effectivelyCloaked", new Cache(() => true))
    squad(Vector(zealot), darkTemplar).scour()
    staysAtPost(zealot)
  }
  check("withdrawal retains its defensive formation and destination") {
    val zealot = unit(Protoss.Zealot)
    val dragoon = unit(Protoss.Dragoon)
    val s = squad(Vector(zealot, dragoon), unit(Zerg.Overlord))
    val retreat = Pixel(32, 32)
    s.formations(1) = FormationSimple(FormationStyleDisengage, Map(zealot -> retreat, dragoon -> retreat))
    s.scour()
    staysAtPost(zealot, retreat)
  }
  check("guard mode also removes an impossible pursuit destination") {
    val zealot = unit(Protoss.Zealot)
    val dragoon = unit(Protoss.Dragoon)
    val s = squad(Vector(zealot, dragoon), unit(Zerg.Overlord))
    zealot.intent.setTerminus(pursuit)
    guardWithoutTargets.invoke(s)
    staysAtPost(zealot)
    assert(dragoon.stationEngage.contains(pursuit))
  }
  check("a bastion formation without a guard style still supplies the defensive post") {
    val zealot = unit(Protoss.Zealot)
    val s = squad(Vector(zealot), unit(Zerg.Overlord))
    s.formations(1) = FormationSimple(FormationStyleMarch, Map(zealot -> post))
    s.scour()
    staysAtPost(zealot)
  }
  check("missing defensive slots fall back to home, and absent targets preserve normal orders") {
    val zealot = unit(Protoss.Zealot)
    val s = squad(Vector(zealot), unit(Zerg.Overlord))
    s.formations(1) = FormationSimple(FormationStyleGuard, Map.empty)
    s.scour()
    staysAtPost(zealot, s.homeConsensus)
    s.setTargets(Vector.empty)
    zealot.intent.setTerminus(post)
    guardWithoutTargets.invoke(s)
    assert(zealot.intent.terminus.contains(post))
  }
  println(s"$passed defense squad tests passed")
}
