package bwapi {
  // Constructed with Unsafe: observations and commands needed by these tests only.
  class DropTestUnit extends Unit(null, 0, null) {
    var unloaded: Unit = _
    var queuedScarabs: Int = 0
    var movedTo: Position = _
    var clicked: Unit = _
    var stopped: Boolean = false
    override def rightClick(target: Unit): Boolean = { clicked = target; true }
    override def stop(): Boolean = { stopped = true; true }
    override def move(to: Position): Boolean = { movedTo = to; true }
    override def getTrainingQueueCount: Int = queuedScarabs
    override def build(kind: UnitType): Boolean = {
      if (queuedScarabs >= 5) false else { queuedScarabs += 1; true }
    }
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

  import scala.collection.JavaConverters._
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
    set(With.blackboard, "reaverDrop", new Utilities.Property(true))
    set(With.blackboard, "wantToHarass", new Utilities.Property(true))
    set(With.blackboard, "reaverDropContinuePressure", new Utilities.Property(true))
    With.latency = allocate(classOf[Latency])
    set(With.latency, "_remainingFrames", 1)
    With.reaction = new ReactionTimes { override def sluggishness: Int = 0; override def agencyMax: Int = 1 }
    With.configuration = new Configuration
    With.logger = new Logger { override def debug(message: String): Unit = {}; override def micro(message: String): Unit = {} }
    With.battles = allocate(classOf[Battles])
    With.battles.byUnit = Map.empty
    With.matchups = allocate(classOf[Micro.Matchups.MatchupGraph])
    set(With.matchups, "entrants", mutable.HashMap.empty[Information.Battles.Types.Battle, ArrayBuffer[UnitInfo]])
    With.agents = allocate(classOf[Agency])
    set(With.agents, "actionPerformance", mutable.HashMap.empty[String, ActionPerformance])
    var availableMinerals = 300
    With.self = new PlayerInfo(null) {
      override def minerals: Int = availableMinerals
      override lazy val isNeutral = false
      override lazy val isAlly = false
      override lazy val isUs = true
      override lazy val isEnemy = false
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
      set(With.grids, "lastSeen", new Information.Grids.Vision.GridLastSeen)
      set(With.grids, "walkable", new GridWalkable)
      val terrain = new GridWalkableTerrain
      terrain.indices.foreach(terrain.set(_, true))
      set(With.grids, "walkableTerrain", terrain)
      set(With.grids, "unwalkableUnits", new GridUnwalkableUnits)
      val region = allocate(classOf[bwapi.Region])
      set(region, "id", 0); set(region, "center", Pixel(16, 16).bwapi)
      set(region, "regionGroupID", 1); set(region, "accessible", true)
      set(region, "neighbours", Vector.empty[bwapi.Region].asJava)
      With.game = allocate(classOf[bwapi.Game])
      set(With.game, "mapWidth", size); set(With.game, "mapHeight", size)
      set(With.game, "mapPixelWidth", size * 32); set(With.game, "mapPixelHeight", size * 32)
      set(With.game, "regions", Array(region)); set(With.game, "regionSet", Vector(region).asJava)
      set(With.game, "mapTileRegionID", Array.ofDim[Short](size, size))
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
      set(u, "_pixelObserved", Pixel(x, y)); set(u, "_visibility", ProxyBwapi.UnitTracking.Visibility.Visible)
      set(u, "_alive", true); set(u, "_complete", true); set(u, "_powered", true)
      set(u, "_flying", kind == Protoss.Shuttle); set(u, "_transport", None)
      set(u, "_scarabs", 3); set(u, "_trainingQueue", Vector.empty[UnitClass])
      set(u, "_squad", None); set(u, "friendly", Some(u)); set(u, "damageQueue", ArrayBuffer.empty)
      set(u, "_intent", new Intention)
      set(u, "_loadedUnitsCache", new Cache(() => observations.filter(_.transport.contains(u)).toVector, refreshPeriod = 0))
      set(u, "_cooldownLeft", new Cache(() => 60))
      set(u, "canMoveCache", new Cache(() => true))
      set(u, "topSpeedPossibleCache", new Cache(() => kind.topSpeed))
      set(u, "subjectiveValueCache", new Cache(() => kind.subjectiveValue))
      set(u, "canAttackCache", new Cache(() => kind.rawCanAttack))
      set(u, "attacksAgainstAirCache", new Cache(() => kind.airDamageFactorRaw * kind.maxAirHitsRaw))
      set(u, "attacksAgainstGroundCache", new Cache(() => kind.groundDamageFactorRaw * kind.maxGroundHitsRaw))
      set(u, "pixelRangeAirCache", new Cache(() => kind.pixelRangeAir))
      set(u, "pixelRangeGroundCache", new Cache(() => kind.pixelRangeGround))
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

    def base(x: Int, y: Int = 5): Base = {
      val b = new Base("test", Tile(x, y), Set(Tile(x, y)))
      b.owner = enemy
      val mapping = Array.fill[Option[Base]](With.mapTileArea)(Some(b))
      set(With.geography, "_baseByTile", mapping)
      b
    }
    class MissionProbe(val target: Base) extends MissionReaverDrop {
      var autonomousLanding = false
      override protected def defaultCombatLanding: Boolean = autonomousLanding
      def approach(t: FriendlyUnitInfo): Pixel = landingGoal(t)
      def controlsCombat(t: FriendlyUnitInfo): Boolean = raidingTransports(t)

      override protected def requireWorkers: Boolean = false
      var scoutTargets = false
      override protected def isScoutingTarget(b: Base): Boolean = scoutTargets && super.isScoutingTarget(b)
      override protected def refreshItinerary(): Unit = {}
      var continuePressure = false
      override protected def continueItinerary(from: Base): Boolean = continuePressure && super.continueItinerary(from)
      def continueNow(from: Base): Boolean = continueItinerary(from)
      override protected def populateItinerary(): Unit = { itinerary.clear(); itinerary += target }
      var assessDefense = false
      override protected def shouldStopRaiding: Boolean = assessDefense && super.shouldStopRaiding
      def mustLeave: Boolean = shouldStopRaiding
      def canFormNow: Boolean = shouldForm
      def recruitNow(): Unit = recruit()
      def combatAction: Action = raidTransportAction
      def commits: Boolean = commitWhileRaiding
      def configure(ts: Seq[FriendlyUnitInfo], ps: Seq[FriendlyUnitInfo]): Unit = {
        launchFrame = With.frame
        transports ++= ts; passengers ++= ps; itinerary += target
        (ts ++ ps).foreach(_.setSquad(Some(this)))
        set(this, "_unitsNow", ArrayBuffer((ts ++ ps): _*))
        manifest = DropManifest.fill(ts, ps)
      }
    }
    val first = base(5)
    val second = base(110)
    check("a fresh Reaver queues five Scarabs in one micro update") {
      val r = unit(Protoss.Reaver)
      set(r, "_scarabs", 0)
      Micro.Actions.Protoss.ReloadScarabs(r)
      assert(r.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].queuedScarabs == 5)
    }
    check("Scarab queues respect minerals and count completed ammunition") {
      val r = unit(Protoss.Reaver)
      set(r, "_scarabs", 0)
      availableMinerals = 30
      Micro.Actions.Protoss.ReloadScarabs(r)
      assert(r.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].queuedScarabs == 2)
      availableMinerals = 300
      val stocked = unit(Protoss.Reaver)
      Micro.Actions.Protoss.ReloadScarabs(stocked)
      assert(stocked.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].queuedScarabs == 2)
    }
    check("assembly waits for two completed or queued Scarabs without waiting for completion") {
      val t = unit(Protoss.Shuttle); val r = unit(Protoss.Reaver)
      val m = new MissionProbe(first); m.configure(Vector(t), Vector(r))
      t.agent.addPassenger(r)
      r.sleepUntil = With.frame + 10
      set(r, "_scarabs", 0)
      set(r, "_trainingQueue", Vector(Protoss.Scarab))
      perform(m.ActionAssemblePassenger, r)
      assert(!r.agent.wantsPickup)
      set(r, "_trainingQueue", Vector(Protoss.Scarab, Protoss.Scarab))
      perform(m.ActionAssemblePassenger, r)
      assert(r.agent.wantsPickup)
    }
    check("a loaded Reaver with two queued Scarabs stays aboard to finish them in flight") {
      val t = unit(Protoss.Shuttle); val r = unit(Protoss.Reaver)
      set(r, "_scarabs", 0)
      set(r, "_trainingQueue", Vector(Protoss.Scarab, Protoss.Scarab))
      r.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].queuedScarabs = 2
      load(r, Some(t))
      Micro.Actions.Protoss.ReloadScarabs(r)
      assert(!r.agent.wantsUnload && t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].unloaded == null)
    }
    check("the newly completed second Reaver is recruited before it has Scarabs") {
      val t = unit(Protoss.Shuttle)
      val ready = unit(Protoss.Reaver)
      val fresh = unit(Protoss.Reaver)
      set(fresh, "_scarabs", 0)
      With.recruiter = new Recruiter
      set(With.recruiter, "unlockedUnits", mutable.Set(t, ready, fresh))
      With.blackboard.reaverDropMaxReavers.set(6)
      val m = new MissionProbe(first)
      m.recruitNow()
      assert(m.passengers.toSet == Set(ready, fresh), "Both completed Reavers must occupy the Shuttle")
      assert(m.assignedTransport(fresh).contains(t))
    }
    check("one Shuttle and one Reaver form immediately and depart as soon as loaded") {
      for (alreadyLoaded <- Vector(false, true)) {
        val t = unit(Protoss.Shuttle); val r = unit(Protoss.Reaver)
        set(r, "_scarabs", 0)
        if (alreadyLoaded) {
          set(r, "_trainingQueue", Vector.fill(2)(Protoss.Scarab))
          load(r, Some(t))
        }
        With.recruiter = new Recruiter
        set(With.recruiter, "unlockedUnits", mutable.Set(t, r))
        With.blackboard.reaverDropMaxReavers.set(1)
        val m = new MissionProbe(first)
        assert(m.canFormNow, "A second Reaver must not delay mission formation")
        m.recruitNow()
        assert(m.passengers.toVector == Vector(r) && m.transports.toVector == Vector(t))
        assert(m.assignedTransport(r).contains(t))
        (Vector(t, r)).foreach(_.setSquad(Some(m)))
        set(m, "_unitsNow", ArrayBuffer(t, r))
        m.launchFrame = With.frame
        m.run()
        if (!alreadyLoaded) {
          assert(m.state == m.Assembling)
          perform(m.ActionAssemblePassenger, r)
          assert(!r.agent.wantsPickup)
          set(r, "_trainingQueue", Vector.fill(2)(Protoss.Scarab))
          perform(m.ActionAssemblePassenger, r)
          assert(r.agent.wantsPickup)
          load(r, Some(t))
          m.run()
        }
        assert(m.state == m.Travelling)
        assert(t.intent.action == m.ActionTravelTransport)
      }
    }
    check("a drop still needs an enabled mission and a completed transportable Reaver") {
      val t = unit(Protoss.Shuttle); val r = unit(Protoss.Reaver)
      With.recruiter = new Recruiter
      With.blackboard.reaverDropMaxReavers.set(1)
      val m = new MissionProbe(first)
      for (available <- Vector(mutable.Set(t), mutable.Set(r))) {
        set(With.recruiter, "unlockedUnits", available)
        assert(!m.canFormNow)
      }
      set(With.recruiter, "unlockedUnits", mutable.Set(t, r))
      set(r, "_complete", false)
      assert(!m.canFormNow)
      set(r, "_complete", true)
      With.blackboard.reaverDrop.set(false)
      assert(!m.canFormNow)
      With.blackboard.reaverDrop.set(true)
      With.blackboard.wantToHarass.set(false)
      assert(!m.canFormNow)
      With.blackboard.wantToHarass.set(true)
      assert(m.canFormNow)
    }
    check("a cap of one recruits only one of two completed Reavers on separate Shuttles") {
      val ts = Vector.fill(2)(unit(Protoss.Shuttle))
      val rs = Vector.fill(2)(unit(Protoss.Reaver))
      val zs = Vector.fill(4)(unit(Protoss.Zealot))
      rs.zip(ts).foreach { case (r, t) => load(r, Some(t)) }
      With.recruiter = new Recruiter
      set(With.recruiter, "unlockedUnits", mutable.Set((ts ++ rs ++ zs): _*))
      With.blackboard.reaverDropMaxReavers.set(1)
      val m = new MissionProbe(first)
      assert(m.canFormNow)
      m.recruitNow()
      assert(m.passengers.count(Protoss.Reaver) == 1)
      assert(m.reaverLock.units.size == 1 && m.transports.size == 1)
      assert(With.recruiter.available.count(Protoss.Reaver) == 1)
      assert(m.passengers.count(Protoss.Zealot) == 2 && fits(m.passengers.map(p => p -> m.assignedTransport(p).get).toMap))
    }
    check("drop targets include the enemy main and natural and exclude empty expansions") {
      With.geography.home = Tile(110, 5)
      val main = base(5); main.workerCount = 12
      val natural = base(16); natural.workerCount = 6
      main.natural = Some(natural); natural.naturalOf = Some(main)
      val ours = base(110); ours.owner = With.self
      val empty = base(80); empty.owner = new PlayerInfo(null) { override lazy val isNeutral = true; override lazy val isAlly = false; override lazy val isUs = false; override lazy val isEnemy = false }
      assert(MissionReaverDrop.targets(Vector(empty, ours, natural, main)) == Vector(main, natural))
    }
    check("the unseen enemy natural follows the main before the drop can go home") {
      val main = base(5); main.workerCount = 12; main.lastFrameScoutedByUs = 1
      val natural = base(36)
      val neutral = new PlayerInfo(null) { override lazy val isNeutral = true; override lazy val isAlly = false; override lazy val isUs = false; override lazy val isEnemy = false }
      natural.owner = neutral; main.natural = Some(natural); natural.naturalOf = Some(main)
      With.scouting = allocate(classOf[Information.Scouting.Scouting])
      set(With.scouting, "_firstEnemyMain", Some(main))
      val targets = Vector(main, natural)
      assert(MissionReaverDrop.targets(targets) == targets, "An unseen natural is a scouting stop, not an empty expansion")
      val t = unit(Protoss.Shuttle); val r = unit(Protoss.Reaver)
      val m = new MissionProbe(main) { override protected def targetCleared(b: Base): Boolean = b == main && state == Raiding }
      m.scoutTargets = true; m.configure(Vector(t), Vector(r)); m.appendTargets(targets)
      set(With.geography, "_bases", targets)
      m.updateState(false); m.state = m.Raiding
      m.run()
      assert(m.state == m.Evacuating && m.itinerary.toVector == Vector(natural))
      load(r, Some(t)); m.run()
      assert(m.state == m.Travelling && m.vicinity == natural.heart.center,
        "Workers fleeing the main must lead the drop to the unseen natural instead of home")
      m.run()
      assert(m.state == m.Travelling, "Keep the crew loaded while natural ownership is unknown")
      natural.owner = enemy; m.run()
      assert(m.state == m.Landing, "Finding the enemy natural releases the drop")
      natural.owner = neutral; natural.lastFrameScoutedByUs = With.frame
      assert(MissionReaverDrop.targets(targets) == Vector(main), "A scouted empty natural is not a raid target")
      m.state = m.Travelling; m.run()
      assert(m.state == m.Escaping && r.loaded, "Seeing an empty natural must not strand or unload the crew")
      natural.owner = With.self; natural.lastFrameScoutedByUs = 0
      assert(MissionReaverDrop.targets(targets) == Vector(main), "Never scout a friendly natural as a raid target")
    }
    check("missing scouting selects only unscouted candidate mains, then prefers confirmed bases") {
      val unknown = base(80); val empty = base(60); val confirmed = base(20)
      val neutral = new PlayerInfo(null) { override lazy val isNeutral = true; override lazy val isAlly = false; override lazy val isUs = false; override lazy val isEnemy = false }
      unknown.owner = neutral; empty.owner = neutral; empty.lastFrameScoutedByUs = 50
      assert(MissionReaverDrop.targets(Vector(unknown, empty), Vector(unknown, empty)) == Vector(unknown))
      assert(MissionReaverDrop.targets(Vector(unknown, empty, confirmed), Vector(unknown)) == Vector(confirmed))
      val t = unit(Protoss.Shuttle); val rs = Vector.fill(2)(unit(Protoss.Reaver))
      val m = new MissionProbe(unknown); m.configure(Vector(t), rs); rs.foreach(load(_, Some(t)))
      set(With.geography, "_baseByTile", Array.fill[Option[Base]](With.mapTileArea)(Some(unknown)))
      m.updateState(false)
      assert(m.state == m.Travelling, "An unconfirmed start is scouted with both Reavers aboard")
      unknown.owner = enemy
      m.updateState(false)
      assert(m.state == m.Landing, "Confirmed ownership releases the actual drop")
    }
    check("reverse pressure sweeps back through previous targets with one surviving Reaver") {
      With.geography.home = Tile(0, 5)
      val a = base(20); val b = base(50); val c = base(90)
      assert(MissionReaverDrop.reverseTargets(Vector(a, b, c), c) == Vector(b, a))
      assert(MissionReaverDrop.reverseTargets(Vector(a, b, c), b) == Vector(a, c))
      set(With.geography, "_bases", Vector(a, b, c))
      val t = unit(Protoss.Shuttle); val r = unit(Protoss.Reaver)
      val m = new MissionProbe(c) { override protected def targetCleared(base: Base): Boolean = base == c }
      m.continuePressure = true; m.configure(Vector(t), Vector(r))
      m.itinerary.clear(); m.appendTargets(Vector(a, b, c))
      m.itinerary.clear(); m.itinerary += c
      m.updateState(false); m.state = m.Raiding
      val previousVicinity = m.vicinity
      m.run()
      assert(m.itinerary.toVector == Vector(b, a) && m.state == m.Evacuating)
      assert(m.vicinity == previousVicinity, "Pickup keeps the current base as its goal")
      load(r, Some(t)); m.run()
      assert(m.state == m.Travelling && m.vicinity == b.heart.center)
      With.blackboard.reaverDropContinuePressure.set(false)
      assert(!m.continueNow(b))
      With.blackboard.reaverDropContinuePressure.set(true)
      set(With.geography, "_bases", Vector(b))
      assert(!m.continueNow(b))
      set(With.geography, "_bases", Vector(a, b, c))
      m.transports.clear()
      assert(!m.continueNow(b))
    }
    check("continued pressure renews the mission and gives the next approach its own clock") {
      With.geography.home = Tile(0, 5)
      val a = base(20); val b = base(90)
      set(With.geography, "_bases", Vector(a, b))
      val t = unit(Protoss.Shuttle); val r = unit(Protoss.Reaver)
      val m = new MissionProbe(b)
      m.continuePressure = true; m.configure(Vector(t), Vector(r))
      m.itinerary.clear(); m.appendTargets(Vector(a, b)); m.itinerary.clear(); m.itinerary += b
      m.updateState(false); m.state = m.Raiding
      With.frame += Seconds(181)()
      m.run()
      assert(m.itinerary.toVector == Vector(a) && m.state == m.Evacuating)
      load(r, Some(t)); m.run()
      assert(m.state == m.Travelling && m.vicinity == a.heart.center)
      With.frame += Seconds(60)()
      m.run()
      assert(m.state == m.Travelling, "The original mission age cannot end the renewed approach")
    }
    check("a visible base heart cannot clear workers hidden beyond the approach") {
      val target = base(5)
      val resource = unit(Protoss.Probe, 500, 300)
      target.minerals = Vector(resource)
      assert(!MissionReaverDrop.workersCleared(target, t => t == target.heart))
      assert(MissionReaverDrop.workersCleared(target, _ => true))
      val worker = unit(Protoss.Probe); set(worker, "_player", enemy)
      target.units = Vector(worker)
      assert(!MissionReaverDrop.workersCleared(target, _ => true))
    }
    check("newly discovered enemy bases extend the raid without revisiting cleared targets") {
      val main = base(5); val expansion = base(16)
      val m = new MissionProbe(main)
      m.appendTargets(Vector(main))
      m.appendTargets(Vector(main, expansion))
      assert(m.itinerary.toVector == Vector(main, expansion))
      m.itinerary.dequeue()
      m.appendTargets(Vector(main, expansion))
      assert(m.itinerary.toVector == Vector(expansion))
      m.itinerary.dequeue()
      m.appendTargets(Vector(main, expansion))
      assert(m.itinerary.isEmpty)
      m.reset()
      m.appendTargets(Vector(main))
      assert(m.itinerary.toVector == Vector(main))
    }
    check("a newly encountered worker base is raided before the distant main") {
      val main = base(115); val expansion = base(46); val natural = base(82)
      val t = unit(Protoss.Shuttle, expansion.heart.center.x - 300, expansion.heart.center.y)
      val r = unit(Protoss.Reaver); load(r, Some(t))
      val worker = unit(Protoss.Probe, expansion.heart.center.x, expansion.heart.center.y)
      val naturalWorker = unit(Protoss.Probe, natural.heart.center.x, natural.heart.center.y)
      Vector(worker, naturalWorker).foreach(set(_, "_player", enemy))
      expansion.units = Vector(worker); natural.units = Vector(naturalWorker)
      val m = new MissionProbe(main); m.configure(Vector(t), Vector(r))
      m.updateState(false)
      m.appendTargets(Vector(main, natural, expansion))
      assert(m.itinerary.toVector == Vector(expansion, main, natural), "Do not fly past the workers just discovered")
      m.updateState(false)
      assert(m.vicinity == expansion.heart.center && m.state == m.Travelling)
      m.state = m.Raiding
      set(t, "_pixel", natural.heart.center)
      m.appendTargets(Vector(main, natural, expansion))
      assert(m.itinerary.head == expansion, "Finish the current raid before switching bases")
      m.itinerary.dequeue(); m.state = m.Travelling
      m.appendTargets(Vector(main, natural, expansion))
      assert(m.itinerary.toVector == Vector(natural, main), "Stop at the natural on the following leg too")
      m.appendTargets(Vector(main, natural, expansion))
      assert(m.itinerary.head == natural, "Keep the closer current target")
      val previousBases = With.geography.bases
      set(With.geography, "_bases", Vector(main, natural, expansion))
      m.itinerary.clear(); m.continuePressure = true
      assert(m.continueNow(main) && m.itinerary.toVector == Vector(natural, expansion),
        "The return sweep must reverse the actual visit order")
      set(With.geography, "_bases", previousBases)
    }
    check("the drop build can refuse an unowned natural until expansion is underway") {
      set(With.blackboard, "takeUnownedBases", new Utilities.Property(false))
      val main = base(5); main.owner = With.self
      val natural = base(16)
      natural.owner = new PlayerInfo(null) { override lazy val isNeutral = true; override lazy val isAlly = false; override lazy val isUs = false; override lazy val isEnemy = false }
      assert(Tactic.GetDefenseBase.canSecure(Vector(main)))
      assert(!Tactic.GetDefenseBase.canSecure(Vector(natural)))
      assert(!Tactic.GetDefenseBase.canSecure(Vector.empty))
      natural.lastPlannedExpo = With.frame
      assert(Tactic.GetDefenseBase.canSecure(Vector(natural)))
      natural.lastPlannedExpo = -100000
      With.blackboard.takeUnownedBases.set(true)
      assert(Tactic.GetDefenseBase.canSecure(Vector(natural)))
    }
    check("Shuttle support follows its Reavers instead of a nearby filler Zealot") {
      val t = unit(Protoss.Shuttle); val r = unit(Protoss.Reaver, 300); val z = unit(Protoss.Zealot)
      t.agent.addPassenger(z); t.agent.addPassenger(r)
      assert(Micro.Actions.Protoss.Shuttle.BeShuttle.supportedPassengers(t) == Vector(r))
      assert(Micro.Actions.Protoss.Shuttle.ShuttlePark.exposedPassengers(t) == Vector(r))
    }
    check("a threatened Shuttle stays with its nearby grounded Reaver") {
      val previousMapping = With.geography.allTiles.map(_.base).toArray
      set(With.geography, "_baseByTile", Array.fill[Option[Base]](With.mapTileArea)(None))
      val t = unit(Protoss.Shuttle, 700, 960); val r = unit(Protoss.Reaver, 574, 1070)
      val threat = unit(Protoss.Dragoon, 450, 1100); set(threat, "_player", enemy)
      t.agent.addPassenger(r)
      t.agent.home.set(Pixel(2000, 960))
      t.agent.forces(Debugging.Visualizations.Forces.threat) = Mathematics.Physics.Force(1, -1)
      t.matchups = new MatchupAnalysis(t) {
        override def threats = Vector(threat)
        override def enemies = Vector(threat)
        override def pixelsEntangled = 1.0
        override def framesOfSafety = -1.0
        override def withinSafetyMargin = false
      }
      Micro.Actions.Protoss.Shuttle.ShuttlePark.parkWithPassengers(t, Vector(r))
      val command = t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].movedTo
      assert(command != null)
      val destination = Pixel(command.getX, command.getY)
      assert(destination.pixelDistance(r.pixel) <= 12, s"Shuttle abandoned its Reaver for $destination")
      t.sleepUntil = 0
      set(t, "_pixel", Pixel(1200, 960)); set(t, "_tile", Pixel(1200, 960).tile)
      Micro.Actions.Protoss.Shuttle.ShuttlePark.parkWithPassengers(t, Vector(r))
      val distant = t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].movedTo
      assert(Pixel(distant.getX, distant.getY).pixelDistance(r.pixel) > t.pixelDistanceCenter(r),
        "Keep retreating when still too far away to support the Reaver")
      set(With.geography, "_baseByTile", previousMapping)
    }
    check("delivering a loaded Reaver cannot abandon its grounded partner") {
      val t = unit(Protoss.Shuttle); val ground = unit(Protoss.Reaver, 300); val rider = unit(Protoss.Reaver)
      t.agent.addPassenger(ground); t.agent.addPassenger(rider); load(rider, Some(t))
      val far = Pixel(3000, 3000)
      assert(Micro.Actions.Protoss.Shuttle.BeShuttle.deliveryGoal(t, far).pixelDistance(ground.pixel) <= 33)
      load(ground, Some(t))
      assert(Micro.Actions.Protoss.Shuttle.BeShuttle.deliveryGoal(t, far) == far)
    }
    check("new squads receive their recruited crew on the first update and run once") {
      With.performance = new Performance.PerformanceMonitor
      With.squads = new Tactic.Squads.Squads
      With.recruiter = new Recruiter
      val crew = Vector(unit(Protoss.Shuttle), unit(Protoss.Reaver), unit(Protoss.Reaver))
      set(With.units, "bufferFriendly", new ProxyBwapi.UnitTracking.UnorderedBuffer(crew))
      var runs = 0
      val squad = new Tactic.Squads.Squad {
        override def launch(): Unit = {}
        override def run(): Unit = { runs += 1; assert(units.toSet == crew.toSet) }
      }
      With.recruiter.lockTo(squad.lock, crew)
      With.squads.commission(squad); With.squads.commission(squad)
      val update = classOf[Tactic.Squads.Squads].getDeclaredMethod("onRun", java.lang.Long.TYPE)
      update.setAccessible(true); update.invoke(With.squads, Long.box(100))
      assert(runs == 1 && crew.forall(_.squad.contains(squad)))
      With.units = new UnitTracker
    }
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
      m.launched = true; m.launchFrame = With.frame
      With.frame += Seconds(181)()
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
    check("clearing workers collects both Reavers and proceeds to the next enemy base") {
      val t = unit(Protoss.Shuttle); val rs = Vector.fill(2)(unit(Protoss.Reaver))
      val m = new MissionProbe(first) { override protected def targetCleared(b: Base): Boolean = b == first }
      m.configure(Vector(t), rs); m.itinerary += second
      m.updateState(false); m.state = m.Raiding
      val old = m.vicinity
      m.run()
      assert(m.itinerary.head == second && m.state == m.Evacuating && m.vicinity == old)
      rs.foreach(load(_, Some(t))); m.run()
      assert(m.state == m.Travelling && m.vicinity == second.heart.center)
    }
    check("a perimeter approach does not expire after the old 45-second limit") {
      val t = unit(Protoss.Shuttle); val rs = Vector.fill(2)(unit(Protoss.Reaver))
      val m = new MissionProbe(first); m.configure(Vector(t), rs)
      rs.foreach(load(_, Some(t))); m.updateState(false)
      m.launched = true; m.launchFrame = With.frame - Seconds(50)()
      m.run()
      assert(m.state == m.Travelling)
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
    check("long cross-map flights use the perimeter instead of cutting through the center") {
      world(128)
      val route = new DropRoute
      var from = Tile(112, 20)
      val to = Tile(6, 101)
      val flown = ArrayBuffer.empty[Tile]
      var steps = 0
      while (from != to && steps < 400) {
        val p = route.next(from, to).get
        val i = p.tiles.get.indices.minBy(j => p.tiles.get(j).tileDistanceSquared(from))
        from = p.tiles.get(Math.min(i + 2, p.length - 1))
        flown += from; With.frame += 6; steps += 1
      }
      assert(from == to)
      assert(flown.forall(t => t.tileDistanceFast(Points.tileMiddle) >= 32), s"Route entered the center: ${flown.filter(_.tileDistanceFast(Points.tileMiddle) < 32)}")
    }
    check("known enemy vision is avoided when an unseen cross-map route exists") {
      world(128)
      With.grids.enemyVision.tiles.foreach(t => t.maxDepth = if (t.tile.x >= 25 && t.tile.x <= 100 && t.tile.y >= 30 && t.tile.y <= 100) 5 else 0)
      val route = new DropRoute
      var from = Tile(112, 20)
      val to = Tile(6, 101)
      var steps = 0
      while (from != to && steps < 400) {
        val p = route.next(from, to).get
        assert(p.tiles.get.forall(t => !t.visibleToEnemy), "An unseen perimeter route exists")
        val i = p.tiles.get.indices.minBy(j => p.tiles.get(j).tileDistanceSquared(from))
        from = p.tiles.get(Math.min(i + 2, p.length - 1))
        With.frame += 6; steps += 1
      }
      assert(from == to)
    }
    check("clear flight legs get long waypoints while turns preserve the safe route") {
      world(128)
      val route = new DropRoute
      val from = Tile(10, 2)
      val p = route.next(from, Tile(118, 2)).get
      assert(route.waypoint(from.center, p).pixelDistance(from.center) >= 288)
      With.grids.enemyVision.tiles.foreach(t => t.maxDepth = if (t.tile.x >= 25 && t.tile.x <= 100 && t.tile.y >= 30 && t.tile.y <= 100) 5 else 0)
      val q = route.next(Tile(112, 20), Tile(6, 101)).get
      for (tile <- q.tiles.get.dropRight(1)) {
        val next = route.waypoint(tile.center, q).tile
        val n = Math.max(Math.abs(next.x - tile.x), Math.abs(next.y - tile.y))
        if (n > 0) for (i <- 0 to n) {
          val crossed = Tile(Math.round(tile.x + (next.x - tile.x) * i.toDouble / n).toInt,
            Math.round(tile.y + (next.y - tile.y) * i.toDouble / n).toInt)
          assert(!crossed.visibleToEnemy)
        }
      }
    }
    check("escaping air threats keeps the movement target beyond braking distance") {
      world(128)
      val threats = Vector(Tile(9, 31), Tile(11, 22), Tile(11, 18))
      With.grids.enemyRangeAir.tiles.foreach(t => t.maxDepth = threats.map(threat =>
        Math.max(0, 9 - Math.ceil(Math.sqrt(t.tile.tileDistanceSquared(threat))).toInt)).max)
      for (from <- Vector(Pixel(381, 812), Pixel(387, 812), Pixel(394, 813), Pixel(430, 818))) {
        val route = new DropRoute
        val path = route.next(from.tile, Tile(6, 4)).get
        val next = route.waypoint(from, path)
        assert(from.pixelDistanceSquared(next) >= 280 * 280, s"Braking during escape: $from -> $next")
        val n = Math.max(Math.abs(next.tile.x - from.tile.x), Math.abs(next.tile.y - from.tile.y))
        val depths = (0 to n).map(i => With.grids.enemyRangeAir.get(Tile(
          Math.round(from.tile.x + (next.tile.x - from.tile.x) * i.toDouble / n).toInt,
          Math.round(from.tile.y + (next.tile.y - from.tile.y) * i.toDouble / n).toInt)))
        assert(depths.sliding(2).forall(pair => pair.last <= pair.head), "Acceleration must lead out of anti-air")
      }
    }
    check("vision from every itinerary base is ignored while other vision and anti-air remain hazards") {
      world(128)
      val expansion = new Base("expansion", Tile(46, 123), Set(Tile(46, 123)))
      val main = new Base("main", Tile(115, 119), Set(Tile(115, 119)))
      expansion.owner = enemy; main.owner = enemy
      val mapping = Array.fill[Option[Base]](With.mapTileArea)(None)
      mapping(expansion.heart.i) = Some(expansion); mapping(main.heart.i) = Some(main)
      set(With.geography, "_baseByTile", mapping)
      val source = unit(Protoss.Probe, expansion.heart.center.x, expansion.heart.center.y)
      set(source, "_player", enemy)
      val from = Tile(34, 119)
      val clear = new DropRoute().next(from, main.heart, Set(main, expansion)).get
      val vision = Information.Grids.Floody.FloodyUnit(source, 9, source.tile)
      With.grids.enemyVision.tiles.foreach(t => {
        val depth = Math.max(0, 11 - Math.ceil(Math.sqrt(t.tile.tileDistanceSquared(source.tile))).toInt)
        t.maxDepth = depth
        if (depth > 0) t.depths.add(Information.Grids.Floody.FloodyUnitDepth(vision, depth))
      })
      val route = new DropRoute
      val dodging = route.next(from, main.heart, Set(main)).get
      assert(dodging.tiles != clear.tiles, "The original route detours around the worker base")
      val raiding = route.next(from, main.heart, Set(main, expansion)).get
      assert(raiding.tiles == clear.tiles, "A later itinerary stop must not repel the Shuttle with its vision")
      mapping(source.tile.i) = None
      assert(route.next(from, main.heart, Set(main, expansion)).get.tiles == dodging.tiles,
        "Vision outside the itinerary bases must still affect routing")
      mapping(source.tile.i) = Some(expansion)
      With.grids.enemyRangeAir.tiles.foreach(t => t.maxDepth =
        Math.max(0, 7 - Math.ceil(Math.sqrt(t.tile.tileDistanceSquared(source.tile))).toInt))
      val defended = route.next(from, main.heart, Set(main, expansion)).get
      assert(defended.tiles.get.forall(t => With.grids.enemyRangeAir.get(t) == 0), "Keep avoiding actual anti-air")
    }
    check("new vision at the destination cannot turn the final approach away from the base") {
      world(128)
      val target = new Base("enemy main", Tile(121, 8), Set(Tile(121, 8)))
      target.owner = enemy
      set(With.geography, "_baseByTile", Array.fill[Option[Base]](With.mapTileArea)(Some(target)))
      val route = new DropRoute
      val from = Tile(94, 2)
      route.next(from, target.heart)
      With.grids.enemyVision.tiles.foreach(t => t.maxDepth =
        if (t.tile.tileDistanceSquared(Tile(103, 6)) <= 100 || t.tile.tileDistanceSquared(target.heart) <= 144) 5 else 0)
      val approach = route.next(from, target.heart).get
      assert(approach.length <= 30, s"Unnecessary destination-vision detour: ${approach.length} tiles")
      assert(route.waypoint(from.center, approach) == target.heart.center)
      With.grids.enemyRangeAir.tiles.foreach(t => t.maxDepth = if (t.tile.tileDistanceSquared(Tile(107, 5)) <= 16) 5 else 0)
      val defended = route.next(from, target.heart).get
      assert(defended.tiles.get.forall(t => With.grids.enemyRangeAir.get(t) == 0), "Final approach must still avoid anti-air")
      With.grids.enemyVision.tiles.foreach(_.maxDepth = 0)
      val unseen = new DropRoute().next(from, target.heart).get
      assert(defended.tiles == unseen.tiles, "Destination vision must not lengthen an anti-air detour")
    }
    def combatMission(approaching: Boolean = false): (MissionProbe, FriendlyUnitInfo, FriendlyUnitInfo, FriendlyUnitInfo, FriendlyUnitInfo) = {
      world(128)
      val target = base(100, 100)
      val at = target.heart.center.add(if (approaching) -320 else 0, 0)
      val worker = unit(ProxyBwapi.Races.Terran.SCV, at.x + 144, at.y)
      set(worker, "_player", enemy); target.units = Vector(worker)
      target.minerals = Vector(-64, 64).map(dy =>
        unit(UnitClass(bwapi.UnitType.Resource_Mineral_Field), target.heart.center.x + 96, target.heart.center.y + dy))
      val t = unit(Protoss.Shuttle, at.x, at.y)
      val r = unit(Protoss.Reaver, at.x, at.y)
      val z = unit(Protoss.Zealot, at.x, at.y)
      val m = new MissionProbe(target)
      m.autonomousLanding = true; m.assessDefense = true; m.launched = true
      m.configure(Vector(t), Vector(r, z)); load(r, Some(t)); load(z, Some(t))
      (m, t, r, z, worker)
    }
    check("reaching the target hands a loaded crew to default combat before the first unload") {
      val (m, t, r, z, _) = combatMission()
      m.run()
      assert(m.state == m.Raiding && m.controlsCombat(t))
      assert(Vector(t, r, z).forall(_.intent.action == Idle) && r.loaded && z.loaded)
      assert(t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].unloaded == null)
      load(r, None); m.run(); load(r, Some(t)); m.run()
      assert(m.state == m.Raiding && t.intent.action == Idle && z.loaded,
        "A recovered Reaver cannot restart initial landing to deploy filler")
    }
    check("an isolated worker cannot trigger an uncontested landing before the mineral line") {
      val (m, t, r, z, worker) = combatMission(approaching = true)
      assert(r.inRangeToAttackFrom(worker, t.pixel) && t.pixelDistanceCenter(m.target.heart.center) > 64)
      m.run()
      assert(m.state == m.Landing && !m.controlsCombat(t),
        "Keep flying to the mineral line even when an isolated worker is already in firing range")
      assert(t.intent.action == m.ActionLandTransport && Vector(r, z).forall(_.intent.action == Micro.Actions.Basic.DoNothing))
      assert(m.approach(t) == m.vicinity, "The approach target must not move to the first visible worker")
      t.matchups = new MatchupAnalysis(t) { override def threats = Vector.empty; override def threatsInRange = Vector.empty }
      perform(m.ActionLandTransport, t)
      val moved = t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].movedTo
      assert(moved != null && Pixel(moved.getX, moved.getY).pixelDistance(m.vicinity) < t.pixelDistanceCenter(m.vicinity))
      assert(t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].unloaded == null && r.loaded && z.loaded)
    }
    check("a second Shuttle keeps delivery control until its own loaded crew reaches combat") {
      val (m, t, r, _, _) = combatMission()
      val other = unit(Protoss.Shuttle, 1800, 3280)
      val otherReaver = unit(Protoss.Reaver, 1800, 3280)
      m.transports += other; m.passengers += otherReaver
      Vector(other, otherReaver).foreach(_.setSquad(Some(m))); load(otherReaver, Some(other))
      m.run()
      assert(m.state == m.Landing && m.controlsCombat(t) && !m.controlsCombat(other))
      assert(t.intent.action == Idle && r.intent.action == Idle)
      assert(other.intent.action == m.ActionLandTransport && otherReaver.intent.action == Micro.Actions.Basic.DoNothing)
      set(other, "_pixel", t.pixel); set(other, "_tile", t.tile)
      set(otherReaver, "_pixel", t.pixel); set(otherReaver, "_tile", t.tile)
      m.run()
      assert(m.state == m.Raiding && Vector(other, otherReaver).forall(_.intent.action == Idle))
    }
    check("a Shuttle under anti-air threat may hand off before reaching the target") {
      val (m, t, r, z, _) = combatMission(approaching = true)
      val marine = unit(ProxyBwapi.Races.Terran.Marine, t.pixel.x + 96, t.pixel.y)
      set(marine, "_player", enemy)
      t.matchups = new MatchupAnalysis(t) { override def threatsInRange = Vector(marine) }
      m.run()
      assert(m.state == m.Raiding && Vector(t, r, z).forall(_.intent.action == Idle))
      assert(r.loaded && z.loaded && t.pixelDistanceCenter(m.vicinity) > 64)
    }
    check("every mineral patch needs recent vision before vision alone permits a landing") {
      val (m, t, r, z, _) = combatMission(approaching = true)
      val minerals = m.target.minerals
      With.grids.lastSeen.set(minerals.head.tile, With.frame)
      m.run(); assert(m.state == m.Landing && !m.controlsCombat(t), "Partial mineral-line vision is insufficient")
      With.grids.lastSeen.set(minerals.last.tile, With.frame - Seconds(6)())
      m.run(); assert(m.state == m.Landing && !m.controlsCombat(t), "Old mineral vision is insufficient")
      With.grids.lastSeen.set(minerals.last.tile, With.frame - Seconds(5)())
      m.run()
      assert(m.state == m.Raiding && Vector(t, r, z).forall(_.intent.action == Idle) && r.loaded)
    }
    check("an empty mineral list does not count as full mineral-line vision") {
      val (m, t, r, _, _) = combatMission(approaching = true)
      m.target.minerals = Vector.empty
      m.run()
      assert(m.state == m.Landing && !m.controlsCombat(t) && r.loaded)
    }
    check("the last approach stays loaded until the Shuttle comes within two tiles of its target") {
      val (m, t, r, z, _) = combatMission(approaching = true)
      Vector(t, r, z).foreach(u => { set(u, "_pixel", m.target.heart.center.add(-65, 0)); set(u, "_tile", u.pixel.tile) })
      m.run(); assert(m.state == m.Landing && r.intent.action == Micro.Actions.Basic.DoNothing)
      Vector(t, r, z).foreach(u => { set(u, "_pixel", m.vicinity.add(-64, 0)); set(u, "_tile", u.pixel.tile) })
      m.run()
      assert(m.state == m.Raiding && Vector(t, r, z).forall(_.intent.action == Idle) && r.loaded)
    }
    check("combat receives a bounded attempt despite a large defending army") {
      val (m, t, r, z, _) = combatMission()
      val defenders = Vector.fill(3)(unit(ProxyBwapi.Races.Terran.SiegeTankUnsieged))
      defenders.foreach(set(_, "_player", enemy)); m.target.units ++= defenders
      m.run()
      assert(m.state == m.Raiding && !m.mustLeave,
        "Army totals must not override the existing combat micro before it can act")
      With.frame += Seconds(5)(); assert(!m.mustLeave)
      // Default combat may already have fled beyond the target base before the timeout.
      set(With.geography, "_baseByTile", Array.fill[Option[Base]](With.mapTileArea)(None))
      With.frame += Seconds(2)(); m.run()
      assert(m.state == m.Escaping && m.launched && t.intent.action == m.ActionEscapeTransport)
      assert(Vector(r, z).forall(_.intent.action == Micro.Actions.Basic.DoNothing))
    }
    check("a shot or first deployment refreshes the combat allowance, but repeated hopping does not") {
      val (m, _, r, _, _) = combatMission(); m.run()
      With.frame += Seconds(5)(); load(r, None); assert(!m.mustLeave)
      With.frame += Seconds(5)(); r.lastFrameStartingAttack = With.frame; assert(!m.mustLeave)
      With.frame += Seconds(5)(); load(r, Some(m.transports.head)); assert(!m.mustLeave)
      With.frame += Seconds(2)(); load(r, None); assert(m.mustLeave)
    }
    check("steady small movements toward workers count as combat progress") {
      val (m, t, _, _, _) = combatMission(); m.run()
      (1 to 10).foreach(_ => {
        With.frame += Seconds(1)()
        set(t, "_pixel", t.pixel.add(8, 0)); set(t, "_tile", t.pixel.tile)
        assert(!m.mustLeave, "Do not expire an approach that steadily closes on workers")
      })
    }
    check("an unusable landing approach is abandoned without forcing a passenger to unload") {
      val (m, t, r, _, worker) = combatMission(approaching = true)
      set(worker, "_visibility", ProxyBwapi.UnitTracking.Visibility.InvisibleMissing)
      m.run(); assert(m.state == m.Landing)
      With.frame += Seconds(9)(); m.run()
      assert(m.state == m.Escaping && r.loaded)
      assert(t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].unloaded == null)
    }
    check("losing the last Reaver retains the empty Shuttle for routed withdrawal") {
      val (m, t, r, z, _) = combatMission(); m.run()
      set(r, "_alive", false); set(z, "_alive", false)
      m.run()
      assert(m.launched && m.passengers.isEmpty && m.state == m.Escaping)
      assert(t.intent.action == m.ActionEscapeTransport && m.transports.contains(t))
    }
    check("optional filler cannot hold a prepared Reaver at assembly indefinitely") {
      val (m, t, r, z, _) = combatMission(); load(z, None)
      m.run(); assert(m.state == m.Assembling)
      set(z, "_order", ProxyBwapi.Orders.EnterTransport)
      With.frame += Seconds(4)(); m.run()
      assert(!m.passengers.contains(z) && m.assignedTransport(z).isEmpty)
      assert(z.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].stopped,
        "Released filler must not retain an engine boarding order that redirects the Shuttle")
      assert(m.state == m.Raiding && r.loaded && t.intent.action == Idle)
    }
    check("recovery collects the Reaver first and cancels competing filler boarding orders") {
      val (m, t, r, z, _) = combatMission(); m.run()
      load(r, None); load(z, None); m.itinerary.clear(); m.run()
      set(z, "_order", ProxyBwapi.Orders.EnterTransport)
      perform(m.ActionEvacuatePassenger, z)
      assert(z.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].stopped)
      perform(m.ActionEvacuateTransport, t)
      perform(m.ActionEvacuatePassenger, r)
      assert(t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].clicked == r.bwapiUnit)
      assert(r.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].clicked == t.bwapiUnit)
      With.frame += Seconds(10)(); m.run()
      assert(m.passengers.contains(r) && m.state == m.Evacuating, "Never expire collection of the required Reaver")
      load(r, Some(t)); m.run(); With.frame += Seconds(6)(); m.run()
      assert(!m.passengers.contains(z) && m.state == m.Escaping)
    }
    def turretEscape(): (FriendlyUnitInfo, FriendlyUnitInfo) = {
      world(128)
      val t = unit(Protoss.Shuttle, 3969, 1097)
      set(t, "_angleRadians", Math.PI / 2)
      val turret = unit(ProxyBwapi.Races.Terran.MissileTurret, 3712, 1152)
      set(turret, "_player", enemy)
      t.agent.home.set(Pixel(464, 448))
      t.matchups = new MatchupAnalysis(t) {
        override def threats = Vector(turret)
        override def enemies = Vector(turret)
        override def threatsInRange = threats.filter(_.inRangeToAttack(t))
        override def pixelsEntangled = 32.0
        override def withinSafetyMargin = false
      }
      (t, turret)
    }
    def movement(t: FriendlyUnitInfo): Pixel = {
      val to = t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].movedTo
      assert(to != null, "An escape must issue a movement command")
      Pixel(to.getX, to.getY)
    }
    def avoidsThreat(t: FriendlyUnitInfo, threat: FriendlyUnitInfo, to: Pixel, margin: Int = 64): Unit = {
      val before = threat.pixelDistanceEdge(t)
      val distance = t.pixelDistanceCenter(to)
      assert(distance >= 128, s"Escape waypoint is too short: $to")
      (0 to 64).foreach(i => {
        val at = t.pixel.project(to, distance * i / 64.0)
        assert(threat.pixelDistanceEdge(t, at) >= Math.min(before, threat.pixelRangeAgainst(t) + margin) - 1,
          s"Escape crosses the threat at $at toward $to")
      })
      assert(threat.pixelDistanceEdge(t, to) >= threat.pixelRangeAgainst(t) + margin, s"Escape ends in weapon range at $to")
    }
    check("a Shuttle retreat escapes the turret before turning toward home") {
      val (t, turret) = turretEscape()
      t.agent.forces(Debugging.Visualizations.Forces.travel) = Mathematics.Physics.Force(-1, 0)
      Micro.Actions.Combat.Maneuvering.Retreat(t)
      avoidsThreat(t, turret, movement(t))
      assert(Micro.Actions.Protoss.Shuttle.ShuttleEscape.active(t))
    }
    check("an empty Shuttle withdraws away from the turret after losing its crew") {
      val (t, turret) = turretEscape()
      val m = new MissionProbe(base(5)); m.autonomousLanding = true; m.launched = true
      m.configure(Vector(t), Vector.empty); m.run()
      assert(m.launched && m.state == m.Escaping)
      set(t.agent, "_defaultHome", new Cache(() => Pixel(464, 448)))
      perform(m.ActionEscapeTransport, t)
      avoidsThreat(t, turret, movement(t))
    }
    check("post-pickup mission travel clears anti-air before following the next target") {
      val (t, turret) = turretEscape()
      val target = base(5)
      val r = unit(Protoss.Reaver, t.pixel.x, t.pixel.y)
      val m = new MissionProbe(target); m.configure(Vector(t), Vector(r))
      t.agent.addPassenger(r); load(r, Some(t)); m.updateState(false)
      perform(m.ActionTravelTransport, t)
      avoidsThreat(t, turret, movement(t))
    }
    check("post-pickup flight clears multiple Marines before resuming the mission") {
      world(128)
      val t = unit(Protoss.Shuttle, 1372, 155)
      set(t, "_angleRadians", 175 * 2 * Math.PI / 256 - Math.PI / 2)
      val marines = Vector((1329, 287), (1421, 138), (1349, 267), (1370, 284))
        .map { case (x, y) => unit(ProxyBwapi.Races.Terran.Marine, x, y) }
      marines.foreach(set(_, "_player", enemy))
      t.matchups = new MatchupAnalysis(t) {
        override def threats = marines
        override def threatsInRange = threats.filter(_.inRangeToAttack(t))
      }
      val r = unit(Protoss.Reaver, t.pixel.x, t.pixel.y)
      val m = new MissionProbe(base(5)); m.configure(Vector(t), Vector(r))
      t.agent.addPassenger(r); load(r, Some(t)); m.updateState(false)
      perform(m.ActionTravelTransport, t)
      marines.foreach(avoidsThreat(t, _, movement(t)))
      // A newly revealed Marine blocks the original westbound flight path.
      t.sleepUntil = 0; set(t, "_pixel", Pixel(1310, 148)); set(t, "_tile", t.pixel.tile)
      val western = unit(ProxyBwapi.Races.Terran.Marine, 1071, 152); set(western, "_player", enemy)
      val all = marines :+ western
      t.matchups = new MatchupAnalysis(t) {
        override def threats = all
        override def threatsInRange = threats.filter(_.inRangeToAttack(t))
      }
      perform(m.ActionTravelTransport, t)
      all.foreach(avoidsThreat(t, _, movement(t), margin = 0))
    }
    check("nearby rescue and combat unloading can interrupt a previous Shuttle escape") {
      val (t, _) = turretEscape()
      val r = unit(Protoss.Reaver, t.pixel.x - 80, t.pixel.y)
      t.agent.addPassenger(r)
      Micro.Actions.Combat.Maneuvering.Retreat(t)
      assert(Micro.Actions.Protoss.Shuttle.ShuttleEscape.active(t))
      Micro.Agency.Commander.rightClick(r, t)
      assert(r.agent.wantsPickup && r.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].clicked == t.bwapiUnit,
        "A nearby Reaver rescue must not be blocked by a previous escape")
      t.sleepUntil = 0
      Micro.Agency.Commander.rightClick(t, r)
      assert(t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].clicked == r.bwapiUnit)
      load(r, Some(t)); r.agent.wantsUnload = true
      t.agent.run = 0; t.sleepUntil = With.frame + 10
      perform(Transport, t)
      assert(t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].unloaded == r.bwapiUnit,
        "The normal combat unload scheduler remains authoritative")
    }
    check("a Shuttle near the map edge escapes along the edge without crossing the turret") {
      val (t, turret) = turretEscape()
      set(t, "_pixel", Pixel(4025, 1247)); set(t, "_tile", t.pixel.tile)
      Micro.Actions.Combat.Maneuvering.Retreat(t)
      avoidsThreat(t, turret, movement(t))
      assert(movement(t).valid)
    }
    check("a distant pickup cannot pull an empty Shuttle back across a turret after escaping") {
      val (t, turret) = turretEscape()
      set(t, "_pixel", Pixel(4020, 1240)); set(t, "_tile", t.pixel.tile)
      set(t, "_angleRadians", 91 * 2 * Math.PI / 256 - Math.PI / 2)
      val r = unit(Protoss.Reaver, 952, 512); t.agent.addPassenger(r)
      With.grids.enemyRangeAir.tiles.foreach(tile => tile.maxDepth =
        Math.max(0, 12 - Math.ceil(tile.tile.center.pixelDistance(turret.pixel) / 32).toInt))
      Micro.Agency.Commander.rightClick(r, t)
      assert(r.agent.wantsPickup && r.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].clicked == null)
      val follower = unit(Protoss.Zealot, r.pixel.x, r.pixel.y)
      Micro.Agency.Commander.rightClick(follower, t)
      assert(follower.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].clicked == null)
      Micro.Agency.Commander.rightClick(t, r)
      assert(Micro.Actions.Protoss.Shuttle.ShuttleEscape.active(t))
      avoidsThreat(t, turret, movement(t))
      set(t, "_pixel", movement(t)); set(t, "_tile", t.pixel.tile)
      assert(!Micro.Actions.Protoss.Shuttle.ShuttleEscape.active(t))
      r.sleepUntil = 0; set(r, "_order", ProxyBwapi.Orders.EnterTransport)
      Micro.Agency.Commander.rightClick(r, t)
      assert(r.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].stopped,
        "A distant EnterTransport order must stay blocked after the escape latch releases")
      var steps = 0
      while (t.pixelDistanceCenter(r) > 384 && steps < 64) {
        t.sleepUntil = 0
        Micro.Agency.Commander.rightClick(t, r)
        assert(t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].clicked == null)
        val to = movement(t)
        val length = t.pixelDistanceCenter(to)
        assert(length > 32, "The routed pickup must make progress")
        (0 to 32).foreach(i => assert(turret.pixelDistanceEdge(t, t.pixel.project(to, length * i / 32)) >
          turret.pixelRangeAgainst(t), s"Pickup flight crosses the turret toward $to"))
        set(t, "_angleRadians", t.pixel.radiansTo(to))
        set(t, "_pixel", t.pixel.project(to, Math.min(128, length))); set(t, "_tile", t.pixel.tile)
        With.frame += 24; steps += 1
      }
      assert(t.pixelDistanceCenter(r) <= 384, "The Shuttle must reach its distant passenger safely")
      t.sleepUntil = 0
      Micro.Agency.Commander.rightClick(t, r)
      assert(t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].clicked == r.bwapiUnit)
    }
    check("nearby combat pickups still issue boarding commands immediately") {
      val (t, _) = turretEscape()
      val r = unit(Protoss.Reaver, t.pixel.x - 100, t.pixel.y)
      t.agent.addPassenger(r)
      Micro.Agency.Commander.rightClick(r, t)
      Micro.Agency.Commander.rightClick(t, r)
      assert(r.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].clicked == t.bwapiUnit)
      assert(t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].clicked == r.bwapiUnit)
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
