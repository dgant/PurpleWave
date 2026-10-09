package bwapi {
  // Constructed with Unsafe: observations and commands needed by these tests only.
  class DropTestUnit extends Unit(null, 0, null) {
    var unloaded: Unit = _
    override def getRemainingBuildTime: Int = 0
    override def getSpaceRemaining: Int = 8
    override def unload(passenger: Unit): Boolean = { unloaded = passenger; true }
  }
}

package Tactic.Missions {
  import Debugging.Logger
  import Information.Battles.Battles
  import Information.Geography.Geography
  import Information.Geography.Pathfinding.Paths
  import Information.Geography.Types.{Base, Zone}
  import Information.Grids.Grids
  import Information.Grids.Floody.{GridEnemyRangeAir, GridEnemyVision}
  import Information.Grids.Movement.{GridUnwalkableUnits, GridWalkable, GridWalkableTerrain}
  import Lifecycle.With
  import Lifecycle.Configure.Configuration
  import Macro.Allocation.{Priorities, Recruiter}
  import Mathematics.Points.{Pixel, Points, Tile}
  import Micro.Actions.{Action, ActionPerformance, Idle}
  import Micro.Actions.Protoss.BeReaver
  import Micro.Actions.Transportation.Transport
  import Micro.Agency.{Agency, Intention}
  import Micro.Matchups.MatchupAnalysis
  import Performance.{Cache, Latency, ReactionTimes}
  import Planning.Blackboard
  import ProxyBwapi.Players.PlayerInfo
  import ProxyBwapi.ProxyBWAPI
  import ProxyBwapi.Races.Protoss
  import ProxyBwapi.UnitClasses.{UnitClass, UnitTypes}
  import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
  import ProxyBwapi.UnitTracking.UnitTracker
  import Utilities.Time.Seconds

  import scala.collection.mutable
  import scala.collection.mutable.ArrayBuffer

  object ReaverDropTest extends App {
    val unsafeField = classOf[sun.misc.Unsafe].getDeclaredField("theUnsafe")
    unsafeField.setAccessible(true)
    val unsafe = unsafeField.get(null).asInstanceOf[sun.misc.Unsafe]
    def allocate[T](clazz: Class[T]): T = unsafe.allocateInstance(clazz).asInstanceOf[T]
    def set(obj: AnyRef, name: String, value: Any): Unit = {
      val fields = Iterator.iterate[Class[_]](obj.getClass)(_.getSuperclass).takeWhile(_ != null).flatMap(_.getDeclaredFields)
        .filter(f => f.getName == name || f.getName.endsWith("$$" + name)).toVector
      assert(fields.nonEmpty, name)
      fields.foreach(f => { f.setAccessible(true); f.set(obj, value.asInstanceOf[AnyRef]) })
    }
    def perform(action: Action, unit: FriendlyUnitInfo): Unit = {
      val method = action.getClass.getDeclaredMethod("perform", classOf[FriendlyUnitInfo])
      method.setAccessible(true)
      method.invoke(action, unit)
    }
    var passed = 0
    def check(name: String)(body: => Unit): Unit = { body; passed += 1; println("PASS " + name) }
    With.frame = 100
    With.proxy = allocate(classOf[ProxyBWAPI])
    set(With.proxy, "unitClasses", UnitTypes.all.map(UnitClass))
    With.units = new UnitTracker
    With.priorities = new Priorities
    With.recruiter = new Recruiter
    With.blackboard = allocate(classOf[Blackboard])
    set(With.blackboard, "reaverDropMaxReavers", new Utilities.Property(1))
    With.latency = allocate(classOf[Latency])
    set(With.latency, "_remainingFrames", 1)
    With.reaction = new ReactionTimes { override def sluggishness: Int = 0 }
    With.configuration = new Configuration
    With.logger = new Logger { override def debug(message: String): Unit = {}; override def micro(message: String): Unit = {} }
    With.battles = allocate(classOf[Battles])
    With.battles.byUnit = Map.empty
    With.matchups = allocate(classOf[Micro.Matchups.MatchupGraph])
    set(With.matchups, "entrants", mutable.HashMap.empty[Information.Battles.Types.Battle, ArrayBuffer[UnitInfo]])
    With.agents = allocate(classOf[Agency])
    set(With.agents, "actionPerformance", mutable.HashMap.empty[String, ActionPerformance])
    With.self = new PlayerInfo(null) {
      override lazy val isNeutral = false
      override lazy val isAlly = false
      override lazy val isUs = true
    }
    val enemy = new PlayerInfo(null) {
      override lazy val isNeutral = false
      override lazy val isAlly = false
      override lazy val isUs = false
      override lazy val isEnemy = true
    }
    def world(size: Int): Unit = {
      With.mapTileWidth = size; With.mapTileHeight = size; With.mapTileArea = size * size
      With.mapPixelWidth = size * 32; With.mapPixelHeight = size * 32
      With.geography = allocate(classOf[Geography])
      set(With.geography, "allTiles", Vector.tabulate(size * size)(i => new Tile(i)))
      set(With.geography, "_baseByTile", Array.fill[Option[Base]](size * size)(None))
      set(With.geography, "_zoneByTile", Array.fill[Zone](size * size)(null))
      With.grids = allocate(classOf[Grids])
      set(With.grids, "enemyRangeAir", new GridEnemyRangeAir)
      set(With.grids, "enemyVision", new GridEnemyVision)
      set(With.grids, "walkable", new GridWalkable)
      val terrain = new GridWalkableTerrain
      terrain.indices.foreach(terrain.set(_, true))
      set(With.grids, "walkableTerrain", terrain)
      set(With.grids, "unwalkableUnits", new GridUnwalkableUnits)
      With.paths = new Paths
    }
    world(128)
    var nextId = 0
    val observations = ArrayBuffer.empty[FriendlyUnitInfo]
    def unit(kind: UnitClass, x: Int = 160, y: Int = 160): FriendlyUnitInfo = {
      nextId += 1
      val u = allocate(classOf[FriendlyUnitInfo])
      set(u, "id", nextId); set(u, "hashCode", nextId)
      set(u, "bwapiUnit", allocate(classOf[bwapi.DropTestUnit]))
      set(u, "_unitClass", kind); set(u, "_player", With.self)
      set(u, "_pixel", Pixel(x, y)); set(u, "_tile", Pixel(x, y).tile)
      set(u, "_alive", true); set(u, "_complete", true); set(u, "_powered", true)
      set(u, "_flying", kind == Protoss.Shuttle); set(u, "_transport", None)
      set(u, "_scarabs", 3); set(u, "_trainingQueue", Vector.empty[UnitClass])
      set(u, "_squad", None); set(u, "friendly", Some(u)); set(u, "damageQueue", ArrayBuffer.empty)
      set(u, "_intent", new Intention)
      set(u, "_loadedUnitsCache", new Cache(() => observations.filter(_.transport.contains(u)).toVector, refreshPeriod = 0))
      set(u, "_cooldownLeft", new Cache(() => 60))
      set(u, "canMoveCache", new Cache(() => true))
      u.matchups = new MatchupAnalysis(u) {
        override def threatsInRange = Vector.empty
        override def targetsInRange = Vector.empty
        override def targetedByScarab = false
      }
      observations += u
      u
    }
    def load(p: FriendlyUnitInfo, t: Option[FriendlyUnitInfo]): Unit = {
      set(p, "_loaded", t.isDefined); set(p, "_transport", t)
    }
    def fits(manifest: DropManifest.Manifest): Boolean = manifest.groupBy(_._2).forall { case (t, crew) =>
      crew.keys.map(_.unitClass.spaceRequired).sum <= t.unitClass.spaceProvided
    }
    check("Reavers scale to available Shuttle seats and the configured maximum") {
      val shuttles = Vector.fill(3)(unit(Protoss.Shuttle))
      val reavers = Vector.fill(9)(unit(Protoss.Reaver))
      for (limit <- Vector(0, 1, 3, 5, 9)) {
        val manifest = DropManifest.reavers(shuttles, reavers, limit)
        assert(manifest.size == Math.min(limit, 6))
        assert(fits(manifest))
      }
    }
    check("matching finds the maximum feasible load with proximity and physical-load constraints") {
      val random = new scala.util.Random(7)
      for (_ <- 0 until 80) {
        val ts = Vector.fill(3)(unit(Protoss.Shuttle, random.nextInt(1800)))
        val ps = Vector.fill(6)(unit(Protoss.Reaver, random.nextInt(1800)))
        def maximum(i: Int, room: Vector[Int]): Int = {
          if (i == ps.size) 0 else {
            val board = ts.indices.filter(j => room(j) > 0 && ps(i).pixelDistanceCenter(ts(j)) < 640)
              .map(j => 1 + maximum(i + 1, room.updated(j, room(j) - 1)))
            (board :+ maximum(i + 1, room)).max
          }
        }
        val manifest = DropManifest.reavers(ts, ps, 6)
        assert(manifest.size == maximum(0, Vector(2, 2, 2)))
        assert(fits(manifest))
      }
      val a = unit(Protoss.Shuttle); val b = unit(Protoss.Shuttle)
      val loaded = unit(Protoss.Reaver); load(loaded, Some(b))
      assert(DropManifest.reavers(Vector(a, b), Vector(loaded), 1)(loaded) == b)
      assert(DropManifest.reavers(Vector(a), Vector(loaded), 1).isEmpty)
    }
    check("filler uses only remaining seats and cannot displace recruited Reavers") {
      val t = unit(Protoss.Shuttle)
      val rs = Vector.fill(2)(unit(Protoss.Reaver))
      val zs = Vector.fill(3)(unit(Protoss.Zealot))
      zs.foreach(load(_, Some(t)))
      val reserved = DropManifest.reavers(Vector(t), rs, 2)
      val full = DropManifest.fill(Vector(t), rs ++ zs, reserved, nearby = true)
      assert(full.keySet == rs.toSet && fits(full))
      zs.foreach(load(_, None))
      val withFiller = DropManifest.fill(Vector(t), rs.take(1) ++ zs ++ Vector(unit(Protoss.Dragoon)), reserved.filterKeys(_ == rs.head), nearby = true)
      assert(withFiller.size == 3 && withFiller.keys.count(Protoss.Reaver) == 1 && fits(withFiller))
    }
    check("rides stay stable and losing capacity retains Reavers before filler") {
      val ts = Vector.fill(2)(unit(Protoss.Shuttle))
      val rs = Vector.fill(2)(unit(Protoss.Reaver))
      val zs = Vector.fill(2)(unit(Protoss.Zealot))
      val original = Map(rs(0) -> ts(1), rs(1) -> ts(0), zs(0) -> ts(0), zs(1) -> ts(1))
      assert(DropManifest.fill(ts, rs ++ zs, original) == original)
      val reduced = DropManifest.fill(ts.take(1), rs ++ zs, original)
      assert(reduced.keySet == rs.toSet && fits(reduced))
    }

    def base(x: Int): Base = {
      val b = new Base("test", Tile(x, 5), Set(Tile(x, 5)))
      b.owner = enemy
      val mapping = Array.fill[Option[Base]](With.mapTileArea)(Some(b))
      set(With.geography, "_baseByTile", mapping)
      b
    }
    class MissionProbe(val target: Base) extends MissionReaverDrop {
      override protected def populateItinerary(): Unit = { itinerary.clear(); itinerary += target }
      override protected def shouldStopRaiding: Boolean = false
      def recruitNow(): Unit = recruit()
      def combatAction: Action = raidTransportAction
      def commits: Boolean = commitWhileRaiding
      def configure(ts: Seq[FriendlyUnitInfo], ps: Seq[FriendlyUnitInfo]): Unit = {
        transports ++= ts; passengers ++= ps; itinerary += target
        (ts ++ ps).foreach(_.setSquad(Some(this)))
        set(this, "_unitsNow", ArrayBuffer((ts ++ ps): _*))
        manifest = DropManifest.fill(ts, ps)
      }
    }
    val first = base(5)
    val second = base(110)
    check("actual recruitment locks multiple Shuttles and survives recruiter renewal") {
      val ts = Vector.fill(3)(unit(Protoss.Shuttle))
      val rs = Vector.fill(5)(unit(Protoss.Reaver))
      val zs = Vector.fill(4)(unit(Protoss.Zealot))
      With.recruiter = new Recruiter
      set(With.recruiter, "unlockedUnits", mutable.Set((ts ++ rs ++ zs): _*))
      With.blackboard.reaverDropMaxReavers.set(5)
      val mission = new MissionProbe(first)
      mission.recruitNow()
      assert(mission.transports.size == 3 && mission.passengers.count(Protoss.Reaver) == 5)
      assert(mission.passengers.count(Protoss.Zealot) == 2)
      rs.foreach(r => { set(r, "_scarabs", 0); set(r, "_pixel", Pixel(3000, 3000)) })
      With.recruiter.renew(mission)
      With.recruiter.update()
      assert(mission.reaverLock.units.size == 5 && With.recruiter.lockedBy(mission).size == 10)
    }
    check("raid intents use normal micro with stable, separate Shuttle crews") {
      val ts = Vector.fill(2)(unit(Protoss.Shuttle))
      val rs = Vector.fill(4)(unit(Protoss.Reaver))
      val m = new MissionProbe(first); m.configure(ts, rs); m.state = m.Raiding
      m.run()
      assert((ts ++ rs).forall(_.intent.action == Idle))
      assert(rs.forall(r => !r.agent.commit && r.agent.ride == m.assignedTransport(r)))
      assert(ts.forall(_.agent.passengerSize == 8))
      val rides = rs.map(_.agent.ride)
      m.run()
      assert(rs.map(_.agent.ride) == rides)
    }
    check("expiration keeps the mission active through pickup and return") {
      val t = unit(Protoss.Shuttle); val r = unit(Protoss.Reaver)
      val m = new MissionProbe(first); m.configure(Vector(t), Vector(r)); m.state = m.Raiding
      m.launched = true; m.launchFrame = With.frame - Seconds(91)()
      m.run()
      assert(m.launched && m.state == m.Evacuating)
      assert(t.intent.action == m.ActionEvacuateTransport && r.intent.action == m.ActionEvacuatePassenger)
      load(r, Some(t)); m.run()
      assert(m.launched && m.state == m.Escaping)
      assert(t.intent.action == m.ActionEscapeTransport)
    }
    check("retargeting waits for pickup and preserves the old vicinity until departure") {
      val t = unit(Protoss.Shuttle); val r = unit(Protoss.Reaver)
      val m = new MissionProbe(first); m.configure(Vector(t), Vector(r))
      m.updateState(false); m.state = m.Raiding
      val old = m.vicinity
      m.itinerary.clear(); m.itinerary += second
      m.updateState(false)
      assert(m.state == m.Evacuating && m.vicinity == old)
      load(r, Some(t)); m.updateState(false)
      assert(m.state == m.Travelling && m.vicinity == second.heart.center)
    }
    check("empty itineraries and return requests evacuate before escaping") {
      for (empty <- Vector(false, true)) {
        val t = unit(Protoss.Shuttle); val r = unit(Protoss.Reaver)
        val m = new MissionProbe(first); m.configure(Vector(t), Vector(r))
        m.updateState(false); m.state = m.Landing
        if (empty) m.itinerary.clear()
        m.updateState(!empty)
        assert(m.state == m.Evacuating)
        load(r, Some(t)); m.updateState(false)
        assert(m.state == m.Escaping)
        load(r, None); m.updateState(false)
        assert(m.state == m.Evacuating)
      }
    }
    check("staggered initial landings finish even when an early Reaver hops aboard") {
      val ts = Vector.fill(2)(unit(Protoss.Shuttle))
      val rs = Vector.fill(2)(unit(Protoss.Reaver))
      val m = new MissionProbe(first); m.configure(ts, rs)
      m.updateState(false); m.state = m.Landing
      load(rs(1), Some(ts(1))); m.updateState(false)
      assert(m.state == m.Landing)
      load(rs(0), Some(ts(0))); load(rs(1), None); m.updateState(false)
      assert(m.state == m.Raiding)
      assert(m.combatAction == Idle && !m.commits)
    }
    check("normal Reaver cooldown pickup and transport unloading remain available") {
      val t = unit(Protoss.Shuttle); val r = unit(Protoss.Reaver)
      t.agent.addPassenger(r)
      r.sleepUntil = With.frame + 10
      perform(BeReaver, r)
      assert(r.agent.wantsPickup)
      load(r, Some(t)); r.agent.wantsUnload = true
      t.agent.run = 0; t.sleepUntil = With.frame + 10
      perform(Transport, t)
      assert(t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].unloaded == r.bwapiUnit)
    }
    check("assembly and evacuation passengers already aboard never request unloading") {
      val t = unit(Protoss.Shuttle); val r = unit(Protoss.Reaver)
      val m = new MissionProbe(first); m.configure(Vector(t), Vector(r))
      load(r, Some(t))
      perform(m.ActionAssemblePassenger, r)
      perform(m.ActionEvacuatePassenger, r)
      assert(!r.agent.wantsUnload)
    }
    for (size <- Vector(96, 128); hazard <- Vector(false, true)) {
      world(size)
      With.grids.enemyRangeAir.tiles.foreach(t => t.maxDepth = if (hazard) Math.max(0, 11 - t.tile.tileDistanceFast(Points.tileMiddle).toInt) else 0)
      With.grids.enemyVision.tiles.foreach(t => t.maxDepth = if (hazard) Math.max(0, 13 - t.tile.tileDistanceFast(Points.tileMiddle).toInt) else 0)
      check(s"production rolling routes reach three destinations on $size tiles, air threat=$hazard") {
        for (to <- Vector(Tile(size - 10, size - 10), Tile(size - 10, 10), Tile(size - 26, size - 26))) {
          val route = new DropRoute
          var from = Tile(10, 10)
          var steps = 0
          while (from != to && steps < 400) {
            val path = route.next(from, to).getOrElse(sys.error(s"No route at $from toward $to"))
            assert(path.tiles.get.forall(With.grids.enemyRangeAir.get(_) == 0))
            val index = path.tiles.get.indices.minBy(i => path.tiles.get(i).tileDistanceSquared(from))
            from = path.tiles.get(Math.min(index + 2, path.length - 1))
            With.frame += 6; steps += 1
          }
          assert(from == to, s"Stalled at $from toward $to")
        }
      }
    }
    check("routes are independent and invalidate on air threats, vision, and destination changes") {
      world(128)
      val a = new DropRoute; val b = new DropRoute
      val from = Tile(10, 10); val to = Tile(118, 10)
      val pa = a.next(from, to).get
      val pb = b.next(Tile(10, 118), to).get
      assert(pa.start != pb.start)
      assert(a.next(from, to).get eq pa)
      val tile = pa.tiles.get(5)
      With.grids.enemyRangeAir.tiles(tile.i).maxDepth = 5
      val threatPath = a.next(from, to).get
      assert(!(threatPath eq pa))
      With.grids.enemyVision.tiles(threatPath.tiles.get(3).i).maxDepth = 3
      assert(!(a.next(from, to).get eq threatPath))
      assert(a.next(from, Tile(10, 118)).get.end != pa.end)
    }
    check("a motionless route fails closed and eventually reports a stall") {
      world(128)
      val route = new DropRoute
      val from = Tile(10, 10); val to = Tile(118, 118)
      assert(route.next(from, to).nonEmpty)
      With.frame += Seconds(9)()
      assert(route.next(from, to).isEmpty)
      With.frame += Seconds(8)()
      assert(route.next(from, to).isEmpty && route.stalled)
    }
    println(s"$passed Reaver drop checks passed")
  }
}
