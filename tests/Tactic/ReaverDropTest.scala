package bwapi {
  class DropTestVision {
    val data = new ClientData
    data.setBuffer(new WrappedBuffer(5 * 1024 * 1024))
    def visible(x: Int, y: Int): scala.Unit = data.gameData().setIsVisible(x, y, true)
  }

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
  import Information.Grids.Floody.{GridEnemyRangeAir, GridEnemyRangeAirGround, GridEnemyRangeGround, GridEnemyVision}
  import Information.Grids.Movement.{GridUnwalkableUnits, GridWalkable, GridWalkableTerrain}
  import Lifecycle.With
  import Lifecycle.Configure.Configuration
  import Macro.Allocation.{Priorities, Recruiter}
  import Mathematics.Points.{Pixel, Points, Tile}
  import Micro.Actions.{Action, ActionPerformance, Idle}
  import Micro.Actions.Combat.Maneuvering.Retreat
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
    def performIntent(unit: FriendlyUnitInfo): Unit = {
      unit.agent.forces.clear()
      unit.agent.resetDestinations()
      unit.intent.action(unit)
    }
    var passed = 0
    def check(name: String)(body: => Unit): Unit = { body; passed += 1; println("PASS " + name) }
    With.frame = 100
    With.proxy = allocate(classOf[ProxyBWAPI])
    set(With.proxy, "unitClasses", UnitTypes.all.map(UnitClass))
    set(With.proxy, "upgrades", ProxyBwapi.Upgrades.UpgradeTypes.all.map(ProxyBwapi.Upgrades.Upgrade))
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
    With.reaction = new ReactionTimes { override def sluggishness: Int = 0; override def agencyMax: Int = 1; override def agencyAverage: Int = 1 }
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
      override def getUpgradeLevel(upgrade: ProxyBwapi.Upgrades.Upgrade): Int = 0
      override def isZerg: Boolean = false
      override def minerals: Int = availableMinerals
      override lazy val isNeutral = false
      override lazy val isAlly = false
      override lazy val isUs = true
      override lazy val isEnemy = false
    }
    val enemy = new PlayerInfo(null) {
      override def getUpgradeLevel(upgrade: ProxyBwapi.Upgrades.Upgrade): Int = 0
      override lazy val isNeutral = false
      override lazy val isAlly = false
      override lazy val isUs = false
      override lazy val isEnemy = true
    }
    var vision: bwapi.DropTestVision = _
    def world(size: Int, height: Int = 0): Unit = {
      val mapHeight = if (height > 0) height else size
      With.mapTileWidth = size; With.mapTileHeight = mapHeight; With.mapTileArea = size * mapHeight
      With.mapPixelWidth = size * 32; With.mapPixelHeight = mapHeight * 32
      With.geography = allocate(classOf[Geography])
      set(With.geography, "allTiles", Vector.tabulate(size * mapHeight)(i => new Tile(i)))
      set(With.geography, "_baseByTile", Array.fill[Option[Base]](size * mapHeight)(None))
      set(With.geography, "_zoneByTile", Array.fill[Zone](size * mapHeight)(null))
      With.grids = allocate(classOf[Grids])
      set(With.grids, "enemyRangeAir", new GridEnemyRangeAir)
      set(With.grids, "enemyRangeGround", new GridEnemyRangeGround)
      set(With.grids, "enemyRangeAirGround", new GridEnemyRangeAirGround)
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
      vision = new bwapi.DropTestVision
      set(With.game, "clientData", vision.data)
      set(With.game, "mapWidth", size); set(With.game, "mapHeight", mapHeight)
      set(With.game, "mapPixelWidth", size * 32); set(With.game, "mapPixelHeight", mapHeight * 32)
      set(With.game, "regions", Array(region)); set(With.game, "regionSet", Vector(region).asJava)
      set(With.game, "mapTileRegionID", Array.ofDim[Short](size, mapHeight))
      With.paths = new Paths
      With.coordinator = new Micro.Coordination.Coordinator
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
      set(u, "_hitPoints", kind.maxHitPoints); set(u, "_shieldPoints", kind.maxShields)
      set(u, "_alive", true); set(u, "_complete", true); set(u, "_powered", true)
      set(u, "_flying", kind.isFlyer); set(u, "_transport", None)
      set(u, "_scarabs", 3); set(u, "_trainingQueue", Vector.empty[UnitClass])
      set(u, "_squad", None); set(u, "friendly", Some(u)); set(u, "damageQueue", ArrayBuffer.empty)
      set(u, "_intent", new Intention)
      set(u, "_loadedUnitsCache", new Cache(() => observations.filter(_.transport.contains(u)).toVector, refreshPeriod = 0))
      set(u, "_cooldownLeft", new Cache(() => 60))
      set(u, "canMoveCache", new Cache(() => true))
      set(u, "sightRangePixelsCache", new Cache(() => kind.sightPixels))
      set(u, "topSpeedPossibleCache", new Cache(() => kind.topSpeed))
      set(u, "subjectiveValueCache", new Cache(() => kind.subjectiveValue))
      set(u, "canAttackCache", new Cache(() => kind.rawCanAttack))
      set(u, "canBeAttackedCache", new Cache(() => true))
      set(u, "_effectivelyCloaked", new Cache(() => false))
      set(u, "damageOnHitAirCache", new Cache(() => kind.effectiveAirDamage))
      set(u, "damageOnHitGroundCache", new Cache(() => kind.effectiveGroundDamage))
      set(u, "attacksAgainstAirCache", new Cache(() => kind.airDamageFactorRaw * kind.maxAirHitsRaw))
      set(u, "attacksAgainstGroundCache", new Cache(() => kind.groundDamageFactorRaw * kind.maxGroundHitsRaw))
      set(u, "pixelRangeAirCache", new Cache(() => kind.pixelRangeAir))
      set(u, "pixelRangeGroundCache", new Cache(() => kind.pixelRangeGround))
      set(u.agent, "_defaultHome", new Cache(() => u.pixel))
      u.matchups = new MatchupAnalysis(u) {
        override def enemies = Vector.empty
        override def threatsInRange = Vector.empty
        override def targetsInRange = Vector.empty
        override def targetedByScarab = false
        override def withinSafetyMargin = true
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
      def updateState(goHome: Boolean): Unit = {
        val method = classOf[MissionDrop].getDeclaredMethods.find(m => m.getName.endsWith("updateState") && m.getParameterCount == 1).get
        method.setAccessible(true)
        method.invoke(this, java.lang.Boolean.valueOf(goHome))
      }
      def approach(t: FriendlyUnitInfo): Pixel = landingGoal(t)
      override def appendTargets(bases: Seq[Base], possibleMains: Seq[Base] = Vector.empty): Unit = super.appendTargets(bases, possibleMains)
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
        DropManifest.fill(ts, ps).foreach { case (p, t) => t.agent.addPassenger(p) }
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
    check("general micro lands safely to refill even with two Scarabs already queued") {
      val t = unit(Protoss.Shuttle); val r = unit(Protoss.Reaver)
      set(r, "_scarabs", 0)
      set(r, "_trainingQueue", Vector(Protoss.Scarab, Protoss.Scarab))
      r.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].queuedScarabs = 2
      load(r, Some(t))
      r.agent.shouldFight = true; r.agent.decision.set(r.pixel)
      r.matchups = new MatchupAnalysis(r) { override def framesOfSafety = 1000.0 }
      Micro.Actions.Protoss.ReloadScarabs(r)
      assert(t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].unloaded == r.bwapiUnit)
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
    check("a one-Reaver cap retains both Reavers already aboard the selected Shuttle") {
      val previousMapping = With.geography.allTiles.map(_.base).toArray
      val ours = base(110); ours.owner = With.self
      val t = unit(Protoss.Shuttle); val r = unit(Protoss.Reaver); val extra = unit(Protoss.Reaver)
      val filler = unit(Protoss.Dragoon)
      Vector(r, extra).foreach(load(_, Some(t)))
      With.recruiter = new Recruiter
      set(With.recruiter, "unlockedUnits", mutable.Set(t, r, extra, filler))
      With.blackboard.reaverDropMaxReavers.set(1)
      val m = new MissionProbe(first); m.recruitNow()
      val recruited = With.recruiter.lockedBy(m).toVector
      recruited.foreach(_.setSquad(Some(m))); set(m, "_unitsNow", ArrayBuffer(recruited: _*))
      assert(m.passengers.toSet == Set(r, extra) && m.reaverLock.units.toSet == Set(r, extra))
      assert(With.recruiter.available.exists(_ == filler) && filler.agent.ride.isEmpty)
      assert(m.passengers.forall(p => m.assignedTransport(p).contains(t)))
      With.recruiter.renew(m); With.recruiter.update()
      m.run(); performIntent(t)
      assert(m.state == m.Travelling && m.passengers.forall(_.loaded))
      assert(m.reaverLock.units.toSet == Set(r, extra), "Renewal must retain the over-cap passenger")
      assert(t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].unloaded == null)
      set(With.geography, "_baseByTile", previousMapping)
    }
    check("a Reaver boarding during assembly displaces and releases unboarded filler") {
      val previousMapping = With.geography.allTiles.map(_.base).toArray
      val ours = base(110); ours.owner = With.self
      val t = unit(Protoss.Shuttle); val r = unit(Protoss.Reaver); val extra = unit(Protoss.Reaver)
      val filler = unit(Protoss.Dragoon)
      load(r, Some(t))
      With.recruiter = new Recruiter
      set(With.recruiter, "unlockedUnits", mutable.Set(t, r, filler))
      With.blackboard.reaverDropMaxReavers.set(1)
      val m = new MissionProbe(first); m.recruitNow()
      val recruited = With.recruiter.lockedBy(m).toVector
      recruited.foreach(_.setSquad(Some(m))); set(m, "_unitsNow", ArrayBuffer(recruited: _*))
      m.run(); assert(m.state == m.Assembling && m.passengers.contains(filler))
      val raidGoal = first.heart.center
      filler.intend(m).setTerminus(raidGoal).setStation(raidGoal).setRedoubt(raidGoal).setBoard(t).setTargets(Vector(r))
      filler.agent.toBoard = Some(t); filler.agent.wantsPickup = true; filler.agent.commit = true
      set(filler, "_order", ProxyBwapi.Orders.EnterTransport)
      load(extra, Some(t)); performIntent(t)
      assert(m.passengers.contains(extra) && t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].unloaded == null)
      m.run()
      assert(m.state == m.Travelling && m.passengers.toSet == Set(r, extra))
      assert(filler.squad.isEmpty && filler.agent.ride.isEmpty && !t.agent.passengers.contains(filler))
      assert(!With.recruiter.lockedBy(m).exists(_ == filler) && With.recruiter.available.exists(_ == filler))
      assert(filler.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].stopped && !filler.agent.wantsPickup && !filler.agent.commit)
      assert(filler.intent.action == Idle && filler.intent.terminus.contains(filler.agent.defaultHome))
      assert(filler.agent.toBoard.isEmpty && filler.intent.toBoard.isEmpty && filler.intent.station.isEmpty &&
        filler.intent.redoubt.isEmpty && filler.intent.targets.isEmpty)
      assert(filler.agent.destinationNext() == filler.agent.defaultHome)
      With.frame += 25; m.run()
      assert(filler.squad.isEmpty && filler.intent.terminus.contains(filler.agent.defaultHome), "Departure must not restore the filler unit's raid orders")
      set(With.geography, "_baseByTile", previousMapping)
    }
    check("ending a failed pickup releases grounded filler and its boarding orders") {
      val t = unit(Protoss.Shuttle); val r = unit(Protoss.Reaver); val filler = unit(Protoss.Zealot)
      val m = new MissionProbe(first); m.configure(Vector(t), Vector(r, filler)); m.launched = true
      With.recruiter.lockTo(m.lock, Vector(t, r, filler))
      filler.intend(m).setBoard(t).setTerminus(first.heart.center)
      filler.agent.toBoard = Some(t); filler.agent.wantsPickup = true
      set(filler, "_order", ProxyBwapi.Orders.Move)
      set(t, "_alive", false); m.run()
      assert(!m.launched && With.recruiter.lockedBy(m).isEmpty)
      assert(filler.squad.isEmpty && filler.agent.ride.isEmpty && filler.intent.toBoard.isEmpty && filler.agent.toBoard.isEmpty)
      assert(filler.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].stopped && !filler.agent.wantsPickup)
      assert(filler.intent.action == Idle && filler.intent.terminus.contains(filler.agent.defaultHome))
    }
    check("extra assembly cargo returns to friendly ground before unloading") {
      val previousMapping = With.geography.allTiles.map(_.base).toArray
      val ours = base(5); ours.owner = With.self
      val t = unit(Protoss.Shuttle, 1000, 1000); val r = unit(Protoss.Reaver, 1000, 1000)
      val extra = unit(Protoss.Dragoon, 1000, 1000)
      val home = Tile(5, 5).center
      set(t.agent, "_defaultHome", new Cache(() => home))
      set(With.geography, "_baseByTile", With.geography.allTiles.map(tile =>
        if (tile.tileDistanceFast(home.tile) < 4) Some(ours) else None).toArray)
      val m = new MissionProbe(first); m.configure(Vector(t), Vector(r))
      Vector(r, extra).foreach(load(_, Some(t)))
      m.run(); performIntent(t)
      assert(m.state == m.Assembling && t.intent.terminus.contains(home), s"Extra cargo became ${m.state} with destination ${t.intent.terminus} instead of $home")
      assert(t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].unloaded == null && t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].movedTo != null)
      set(t, "_pixel", home); set(t, "_tile", home.tile); With.frame += 25; t.sleepUntil = 0
      performIntent(t)
      assert(t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].unloaded == extra.bwapiUnit && m.state == m.Assembling)
      set(With.geography, "_baseByTile", previousMapping)
    }
    check("unplanned cargo carried into combat is retained until recovery") {
      val previousMapping = With.geography.allTiles.map(_.base).toArray
      val target = base(84, 120)
      val t = unit(Protoss.Shuttle, 2680, 4000); val r = unit(Protoss.Reaver, 2680, 4000)
      val extra = unit(Protoss.Reaver, 2680, 4000)
      val m = new MissionProbe(target); m.configure(Vector(t), Vector(r))
      load(r, Some(t)); m.updateState(false); m.state = m.Raiding; m.launched = true
      load(extra, Some(t)); extra.agent.wantsUnload = true
      t.agent.run = 1; t.sleepUntil = With.frame + 10
      perform(Transport, t)
      assert(t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].unloaded == extra.bwapiUnit)
      assert(m.passengers.contains(extra) && With.recruiter.lockedBy(m).exists(_ == extra) && extra.squad.contains(m))
      load(extra, None); m.itinerary.clear(); m.run()
      assert(m.state == m.Evacuating && m.launched, "Recovering the planned Reaver alone is not all aboard")
      With.recruiter.renew(m); With.recruiter.update()
      With.frame += 25; m.run(); t.sleepUntil = 0; performIntent(t)
      assert(m.passengers.contains(extra) && t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].clicked == extra.bwapiUnit)
      load(extra, Some(t)); m.run()
      assert(m.state == m.Escaping && m.launched && m.passengers.forall(_.loaded))
      set(With.geography, "_baseByTile", previousMapping)
    }
    check("drop targets include the enemy main and natural and exclude empty expansions") {
      With.geography.home = Tile(110, 5)
      val main = base(5); main.workerCount = 12
      val natural = base(16); natural.workerCount = 6
      main.natural = Some(natural); natural.naturalOf = Some(main)
      val ours = base(110); ours.owner = With.self
      val empty = base(80); empty.owner = new PlayerInfo(null) { override lazy val isNeutral = true; override lazy val isAlly = false; override lazy val isUs = false; override lazy val isEnemy = false }
      assert(MissionReaverDrop.targets(Vector(empty, ours, natural, main)).toSet == Set(main, natural))
    }
    check("the unseen enemy natural follows the main before the drop can go home") {
      With.geography.home = Tile(0, 5)
      val main = base(5); main.workerCount = 12; main.lastFrameScoutedByUs = 1
      val natural = base(36)
      val neutral = new PlayerInfo(null) { override lazy val isNeutral = true; override lazy val isAlly = false; override lazy val isUs = false; override lazy val isEnemy = false }
      natural.owner = neutral; main.natural = Some(natural); natural.naturalOf = Some(main)
      With.scouting = allocate(classOf[Information.Scouting.Scouting])
      set(With.scouting, "_tugStart", new Performance.Cache(() => Tile(0, 5)))
      set(With.scouting, "_tugEnd", new Performance.Cache(() => Tile(110, 5)))
      set(With.scouting, "_firstEnemyMain", Some(main))
      val targets = Vector(main, natural)
      assert(MissionReaverDrop.targets(targets) == targets, "An unseen natural is a scouting stop, not an empty expansion")
      val t = unit(Protoss.Shuttle); val r = unit(Protoss.Reaver)
      val m = new MissionProbe(main) { override protected def targetCleared(b: Base): Boolean = if (b == main) state == Raiding else super.targetCleared(b) }
      m.scoutTargets = true; m.configure(Vector(t), Vector(r)); m.appendTargets(targets)
      set(With.geography, "_bases", targets)
      m.updateState(false); m.state = m.Raiding
      m.run()
      assert(m.state == m.Evacuating && m.itinerary.toVector == Vector(natural))
      load(r, Some(t)); m.run()
      assert(m.state == m.Travelling && m.vicinity == natural.townHallArea.center,
        "Workers fleeing the main must lead the drop to the unseen natural instead of home")
      m.run()
      assert(m.state == m.Travelling, "Keep the crew loaded while natural ownership is unknown")
      natural.owner = enemy; m.run()
      assert(m.state == m.Landing, "Finding the enemy natural releases the drop")
      natural.owner = neutral; natural.lastFrameScoutedByUs = With.frame
      natural.townHallArea.tiles.foreach(t => vision.visible(t.x, t.y))
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
      a.owner = With.self; c.owner = With.self
      assert(!m.continueNow(b))
      b.owner = With.self; a.owner = enemy
      assert(m.continueNow(b) && m.itinerary.toVector == Vector(a), "One remaining enemy base is enough for a return visit")
      b.owner = enemy; c.owner = enemy
      m.transports.clear()
      assert(!m.continueNow(b))
    }
    check("disabling harassment finishes the sweep and recovers the crew instead of reversing") {
      val previousMapping = With.geography.allTiles.map(_.base).toArray
      val a = base(20); val b = base(90)
      set(With.geography, "_bases", Vector(a, b))
      val t = unit(Protoss.Shuttle); val r = unit(Protoss.Reaver)
      var cleared = false
      val m = new MissionProbe(b) { override protected def targetCleared(base: Base): Boolean = cleared && base == b }
      m.continuePressure = true; m.configure(Vector(t), Vector(r)); m.launched = true
      m.itinerary.clear(); m.appendTargets(Vector(a, b)); m.itinerary.clear(); m.itinerary += b
      m.updateState(false); m.state = m.Raiding
      With.recruiter.lockTo(m.lock, Vector(t, r))
      With.blackboard.wantToHarass.set(false)
      m.run()
      assert(m.launched && m.state == m.Raiding, "Disabling harassment does not interrupt the current stop")
      cleared = true; m.run()
      assert(m.itinerary.isEmpty && m.state == m.Evacuating && m.launched)
      assert(r.intent.action == m.ActionEvacuatePassenger, "Recover the grounded Reaver before terminating")
      load(r, Some(t)); m.run()
      assert(m.state == m.Escaping && m.launched)
      set(With.geography, "_baseByTile", Array.fill[Option[Base]](With.mapTileArea)(None))
      m.run()
      assert(!m.launched && With.recruiter.lockedBy(m).isEmpty)
      assert(Vector(t, r).forall(u => u.squad.isEmpty && u.intent.action == Idle))
      With.blackboard.wantToHarass.set(true)
      set(With.geography, "_baseByTile", previousMapping)
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
      With.geography.home = Tile(0, 5)
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
    check("both traversal directions keep every enemy base in perimeter order") {
      val top = base(62, 5); val right = base(115, 62); val bottom = base(62, 115); val left = base(5, 62)
      val candidates = Vector(bottom, top, left, right)
      assert(MissionReaverDrop.orderedTargets(candidates, top.townHallArea.center, clockwise = true) == Vector(top, right, bottom, left))
      assert(MissionReaverDrop.orderedTargets(candidates, top.townHallArea.center, clockwise = false) == Vector(top, left, bottom, right))
    }
    check("discoveries join the directional sweep without interrupting a raid") {
      With.geography.home = Tile(0, 5)
      val main = base(115); val expansion = base(46); val natural = base(82)
      val t = unit(Protoss.Shuttle, Tile(0, 5).center.x, Tile(0, 5).center.y)
      val r = unit(Protoss.Reaver); load(r, Some(t))
      val m = new MissionProbe(main); m.configure(Vector(t), Vector(r))
      m.appendTargets(Vector(main, natural))
      assert(m.itinerary.toVector == Vector(natural, main), "The first sweep starts with the nearer base")
      m.state = m.Travelling
      m.appendTargets(Vector(main, natural, expansion))
      assert(m.itinerary.toVector == Vector(expansion, natural, main), "New discoveries belong at their position in the sweep")
      m.state = m.Raiding
      val discovered = base(58)
      m.appendTargets(Vector(main, natural, expansion, discovered))
      assert(m.itinerary.toVector == Vector(expansion, discovered, natural, main), "Finish the active raid and insert the next base in order")
      m.itinerary.dequeue(); m.state = m.Travelling
      m.appendTargets(Vector(main, natural, expansion, discovered))
      assert(m.itinerary.toVector == Vector(discovered, natural, main), "Cleared stops stay cleared")
      val previousBases = With.geography.bases
      set(With.geography, "_bases", Vector(main, natural, expansion, discovered))
      m.itinerary.clear(); m.continuePressure = true
      assert(m.continueNow(main) && m.itinerary.toVector == Vector(natural, discovered, expansion),
        "The return sweep reverses the traversal")
      set(With.geography, "_bases", previousBases)
    }
    check("original parking supports every unloaded passenger") {
      for (kind <- Vector(Protoss.Reaver, Protoss.HighTemplar, Protoss.DarkTemplar, Protoss.Archon, Protoss.Zealot, Protoss.Dragoon, Protoss.DarkArchon)) {
        val t = unit(Protoss.Shuttle); val p = unit(kind)
        t.agent.addPassenger(p)
        assert(Micro.Actions.Protoss.Shuttle.ShuttlePark.allowed(t), s"Missing parking support for $kind")
        load(p, Some(t))
        assert(!Micro.Actions.Protoss.Shuttle.ShuttlePark.allowed(t), "Loaded passengers do not need parking protection")
      }
    }
    check("original Hover parking stays with its selected passenger") {
      for (kind <- Vector(Protoss.Reaver, Protoss.HighTemplar, Protoss.DarkTemplar, Protoss.Archon)) {
        val t = unit(Protoss.Shuttle, 800, 800)
        val r = unit(Protoss.Reaver, 800, 800); val p = unit(kind, 1000, 800)
        val crew = Vector(r, p) ++ (if (kind.spaceRequired == 2) Vector(unit(Protoss.Zealot, 800, 1000)) else Vector.empty)
        val m = new MissionProbe(first)
        m.configure(Vector(t), crew)
        (Vector(t) ++ crew).foreach(u => u.matchups = new MatchupAnalysis(u) {
          override def threats = Vector.empty
          override def enemies = Vector.empty
          override def pixelsToThreatRange = None
          override def pixelsEntangled = if (Protoss.Zealot(u)) 200.0 else if (u == p) 100.0 else 0.0
        })
        r.agent.perch.set(r.pixel); r.agent.setRideGoal(r.pixel)
        perform(Micro.Actions.Protoss.Shuttle.BeShuttle, t)
        assert(t.agent.lastAction.contains("Hover"), "The nearby Reaver must supply the Hover quest")
        assert(t.agent.decision().pixelDistance(r.pixel) <= 12, s"Hover left its selected Reaver for $kind")
        assert(t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].movedTo != null)
      }
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
      Micro.Actions.Protoss.Shuttle.ShuttlePark(t)
      val command = t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].movedTo
      assert(command != null)
      val destination = Pixel(command.getX, command.getY)
      val pickup = t.agent.decision()
      assert(pickup.pixelDistance(r.pixel) <= 12, s"Shuttle abandoned its Reaver for $pickup")
      assert(destination.pixelDistance(t.pixel.project(pickup, 288)) <= 1,
        "Parking must keep moving along the passenger's pickup heading without braking")
      t.sleepUntil = 0
      set(t, "_pixel", Pixel(1200, 960)); set(t, "_tile", Pixel(1200, 960).tile)
      Micro.Actions.Protoss.Shuttle.ShuttlePark(t)
      val distant = t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].movedTo
      assert(Pixel(distant.getX, distant.getY).pixelDistance(r.pixel) > t.pixelDistanceCenter(r),
        "Keep retreating when still too far away to support the Reaver")
      set(With.geography, "_baseByTile", previousMapping)
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
    check("expiration keeps the mission active until pickup and departure from the final base") {
      val previousMapping = With.geography.allTiles.map(_.base).toArray
      set(With.geography, "_baseByTile", Array.fill[Option[Base]](With.mapTileArea)(Some(first)))
      val t = unit(Protoss.Shuttle); val r = unit(Protoss.Reaver)
      val m = new MissionProbe(first); m.configure(Vector(t), Vector(r)); m.state = m.Raiding
      m.launched = true; m.launchFrame = With.frame
      m.updateState(false); m.state = m.Raiding
      With.recruiter.lockTo(m.lock, Vector(t, r))
      With.frame += Seconds(181)()
      m.run()
      assert(m.launched && m.state == m.Evacuating)
      assert(t.intent.action == m.ActionEvacuateTransport && r.intent.action == m.ActionEvacuatePassenger)
      load(r, Some(t)); m.run()
      assert(m.launched && m.state == m.Escaping)
      assert(t.intent.action == m.ActionTravelTransport)
      set(With.geography, "_baseByTile", Array.fill[Option[Base]](With.mapTileArea)(None))
      m.run()
      assert(!m.launched && With.recruiter.lockedBy(m).isEmpty)
      assert(Vector(t, r).forall(u => u.intent.action == Idle && u.squad.isEmpty && u.intent.station.isEmpty &&
        u.intent.redoubt.isEmpty && u.intent.targets.isEmpty && u.intent.terminus.contains(u.agent.defaultHome)))
      assert(r.loaded && r.agent.ride.contains(t), "Normal micro retains the recovered crew")
      r.agent.wantsUnload = true; t.agent.run = 0; t.sleepUntil = With.frame + 10
      perform(Transport, t)
      assert(t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].unloaded == r.bwapiUnit,
        "A released Shuttle must honor normal combat unloading instead of retaining its return action")
      set(With.geography, "_baseByTile", previousMapping)
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
    check("flight rays include crossed tiles and both endpoints in every direction") {
      val random = new scala.util.Random(11)
      val rays = Vector((Pixel(430, 818), Pixel(142, 883)), (Pixel(32, 32), Pixel(128, 128)),
        (Pixel(128, 32), Pixel(32, 128)), (Pixel(32, 32), Pixel(32, 32))) ++
        Vector.fill(200)((Pixel(random.nextInt(4096), random.nextInt(4096)), Pixel(random.nextInt(4096), random.nextInt(4096))))
      rays.flatMap { case (a, b) => Vector((a, b), (b, a)) }.foreach { case (a, b) =>
        val ray = Mathematics.Shapes.Ray(a, b).toVector
        assert(ray.head == a && ray.last == b)
        val seen = ray.map(_.tile).toSet
        val samples = Math.max(1, Math.ceil(a.pixelDistance(b) * 2).toInt)
        (0 to samples).foreach(i => assert(seen(Pixel(
          Math.floor(a.x + (b.x - a.x) * i.toDouble / samples).toInt,
          Math.floor(a.y + (b.y - a.y) * i.toDouble / samples).toInt).tile), s"Missed a tile on $a -> $b"))
      }
      val diagonal = Mathematics.Shapes.Ray(Pixel(16, 16), Pixel(48, 48)).map(_.tile).toSet
      assert(Set(Tile(0, 0), Tile(1, 0), Tile(0, 1), Tile(1, 1)).subsetOf(diagonal))
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
      assert(route.waypoint(unit(Protoss.Shuttle, from.center.x, from.center.y), p).get.pixelDistance(from.center) >= 288)
      With.grids.enemyVision.tiles.foreach(t => t.maxDepth = if (t.tile.x >= 25 && t.tile.x <= 100 && t.tile.y >= 30 && t.tile.y <= 100) 5 else 0)
      val q = route.next(Tile(112, 20), Tile(6, 101)).get
      for (tile <- q.tiles.get.dropRight(1)) {
        val next = route.waypoint(unit(Protoss.Shuttle, tile.center.x, tile.center.y), q).get.tile
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
        val t = unit(Protoss.Shuttle, from.x, from.y)
        val waypoint = route.waypoint(t, path)
        waypoint.foreach(t.agent.decision.set)
        if (waypoint.nonEmpty) Micro.Agency.Commander.move(t)
        val command = t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].movedTo
        if (waypoint.isEmpty) assert(command == null, "Unsafe accelerated turns must defer to retreat")
        else {
          val next = Pixel(command.getX, command.getY)
          assert(from.pixelDistanceSquared(next) >= 280 * 280, s"Braking during escape: $from -> $next")
          val samples = Math.ceil(from.pixelDistance(next)).toInt
          val depths = (0 to samples).map(i => With.grids.enemyRangeAir.get(Pixel(
            Math.floor(from.x + (next.x - from.x) * i.toDouble / samples).toInt,
            Math.floor(from.y + (next.y - from.y) * i.toDouble / samples).toInt).tile))
          assert(depths.sliding(2).forall(pair => pair.last <= pair.head), "Acceleration must lead out of anti-air")
        }
      }
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
    check("the Polypoid arrival stays loaded along its clear route to the mineral line") {
      world(128)
      val target = base(4, 116)
      target.heart; set(target, "heart", Tile(5, 117))
      val start = Pixel(46, 3407)
      val t = unit(Protoss.Shuttle, start.x, start.y); val r = unit(Protoss.Reaver, start.x, start.y)
      val m = new MissionProbe(target); m.autonomousLanding = true
      m.configure(Vector(t), Vector(r)); load(r, Some(t)); m.run()
      assert(!m.controlsCombat(t), "An undefended mineral line 376 pixels away is not arrival")
      var steps = 0
      while (!m.controlsCombat(t) && steps < 120) {
        With.frame += 2; t.sleepUntil = 0
        m.run()
        if (!m.controlsCombat(t)) {
          performIntent(t)
          val next = t.pixel.project(movement(t), 8)
          Vector(t, r).foreach(u => { set(u, "_pixel", next); set(u, "_tile", next.tile) })
        }
        assert(r.loaded && t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].unloaded == null)
        steps += 1
      }
      assert(m.controlsCombat(t) && t.pixelDistanceCenter(target.heart.center) <= 32,
        s"Flight must reach the mineral line before handing over at ${t.pixel}")
    }
    check("arrival uses the 32-pixel boundary rather than Shuttle sight range") {
      val (m, t, r, z, _) = combatMission()
      def at(distance: Int): Unit = Vector(t, r, z).foreach(u => {
        val p = m.target.heart.center.add(-distance, 0)
        set(u, "_pixel", p); set(u, "_tile", p.tile)
      })
      at(33); m.run()
      assert(!m.controlsCombat(t) && r.loaded && z.loaded)
      at(32); m.run()
      assert(m.controlsCombat(t) && Vector(t, r, z).forall(_.intent.action == Idle))
    }
    check("nearby anti-air outside weapon range cannot interrupt a clear arrival") {
      val (m, t, r, z, _) = combatMission(approaching = true)
      val marine = unit(ProxyBwapi.Races.Terran.Marine, t.pixel.x, t.pixel.y + 500)
      set(marine, "_player", enemy)
      t.matchups = new MatchupAnalysis(t) {
        override def threats = Vector(marine)
        override def threatsInRange = Vector.empty
        override def withinSafetyMargin = false
      }
      With.grids.enemyRangeAir.tiles(t.tile.i).maxDepth = 1 // Safety margin, with a clear escape toward the goal.
      m.run()
      assert(!m.controlsCombat(t) && r.loaded && z.loaded)
    }
    check("only blocked approaches or safe detours beyond three times direct distance hand off early") {
      for ((wallLength, shouldHandoff) <- Vector((8, false), (17, true), (128, true))) {
        val (m, t, r, z, _) = combatMission(approaching = true)
        val wallX = t.tile.x + 5
        With.grids.enemyRangeAir.tiles.foreach(cell =>
          cell.maxDepth = if (cell.tile.x == wallX && cell.tile.y <= t.tile.y + wallLength) 1 else 0)
        m.run()
        assert(m.controlsCombat(t) == shouldHandoff, s"Wrong handoff for wall length $wallLength")
        assert(r.loaded && z.loaded)
      }
    }
    check("perimeter sweeps include unowned and previously scouted bases before their raid target") {
      world(128)
      val top = base(62, 4); val right = base(120, 62); val bottom = base(62, 120); val left = base(4, 62)
      val neutral = new PlayerInfo(null) { override lazy val isNeutral = true; override lazy val isAlly = false; override lazy val isUs = false; override lazy val isEnemy = false }
      right.owner = neutral; left.owner = neutral; left.lastFrameScoutedByUs = With.frame - 1
      assert(MissionReaverDrop.sweep(Vector(right, bottom, left), Vector(bottom), top.townHallArea.center, clockwise = true) == Vector(right, bottom))
      assert(MissionReaverDrop.sweep(Vector(right, bottom, left), Vector(bottom), top.townHallArea.center, clockwise = false) == Vector(left, bottom))
    }
    check("Crossing Field's first itinerary scouts the top-left backyard before the enemy main") {
      world(128, 96)
      With.geography.home = Tile(122, 67)
      val coordinates = Vector((70, 62), (44, 7), (5, 28), (84, 88), (122, 38), (120, 91), (95, 4), (6, 4), (57, 37), (31, 91), (5, 58), (122, 67))
      val bases = coordinates.map { case (x, y) => base(x, y) }
      val neutral = new PlayerInfo(null) { override lazy val isNeutral = true; override lazy val isAlly = false; override lazy val isUs = false; override lazy val isEnemy = false }
      bases.foreach(b => b.owner = neutral)
      bases(2).owner = enemy; bases(4).owner = With.self; bases(11).owner = With.self
      bases(10).lastFrameScoutedByUs = 1; bases(10).naturalOf = Some(bases(2))
      val m = new MissionProbe(bases(2)); m.appendTargets(bases)
      assert(m.itinerary.toVector == Vector(bases(6), bases(1), bases(7), bases(2)),
        s"Expected the northern perimeter, including the backyard: ${m.itinerary.map(_.townHallTile)}")
    }
    def flightLength(points: Seq[Tile]): Double = points.sliding(2).map(p => Math.sqrt(p.head.tileDistanceSquared(p.last))).sum
    check("flybys keep a straight flight when it already sees every town hall") {
      world(128)
      val from = Tile(116, 8)
      val stops = Vector((Tile(90, 5), 5.5), (Tile(60, 11), 5.5), (Tile(30, 5), 5.5), (Tile(5, 8), 0.0))
      val contacts = DropRoute.flybys(from, stops)
      assert(contacts.forall(_.y == 8), s"Unnecessary bends: $contacts")
      assert(flightLength(from +: contacts) <= 111.01)
      assert(contacts.zip(stops).forall { case (p, (center, radius)) => p.tileDistanceSquared(center) <= radius * radius })
    }
    check("an off-axis peek minimizes both legs instead of diving radially toward the town hall") {
      world(128)
      val from = Tile(110, 12); val townHall = Tile(70, 4); val target = Tile(8, 12)
      val radius = 5.5
      val contact = DropRoute.flybys(from, Vector((townHall, radius), (target, 0.0))).head
      val candidates = With.geography.allTiles.filter(t => t.tileDistanceSquared(townHall) <= radius * radius)
      val optimum = candidates.map(p => flightLength(Vector(from, p, target))).min
      assert(flightLength(Vector(from, contact, target)) <= optimum + 1, s"Excessive bend through $contact")
      assert(contact.tileDistanceSquared(townHall) >= 16, "Only approach far enough to see the town hall")
    }
    check("production flyby paths shorten successive perimeter peeks while retaining safe contacts") {
      world(128, 96)
      val start = Tile(122, 67)
      val stops = Vector((Tile(97, 5), 5.5), (Tile(46, 8), 5.5), (Tile(8, 5), 5.5), (Tile(5, 28), 0.0))
      def flight(lookAhead: Boolean): Vector[Tile] = {
        var from = start
        stops.indices.flatMap(i => {
          val (center, radius) = stops(i)
          val route = new DropRoute
          val p = route.next(from, center, radius, if (lookAhead) stops.drop(i + 1) else Vector.empty).get
          assert(p.end.tileDistanceSquared(center) <= radius * radius + 1)
          from = p.end
          p.tiles.get
        }).toVector
      }
      val flyby = flight(lookAhead = true)
      val radial = flight(lookAhead = false)
      assert(flightLength(flyby) < flightLength(radial), s"Flyby ${flightLength(flyby)} vs radial ${flightLength(radial)}")
      assert(flyby.forall(_.tileDistanceFast(Points.tileMiddle) >= 28), "Scouting must retain the perimeter preference")
    }
    check("a blocked preferred flyby uses another safe part of the sight region") {
      world(128)
      val from = Tile(110, 12); val center = Tile(70, 4); val target = Tile(8, 12)
      val following = Vector((target, 0.0))
      val route = new DropRoute
      val first = route.next(from, center, 5.5, following).get
      With.grids.enemyRangeAir.tiles(first.end.i).maxDepth = 4
      val changed = route.next(from, center, 5.5, following).get
      assert(changed.end != first.end && changed.end.tileDistanceSquared(center) <= 5.5 * 5.5)
      assert(changed.tiles.get.forall(With.grids.enemyRangeAir.get(_) == 0))
      assert(!(route.next(from, center, 5.5, Vector((Tile(100, 80), 0.0))).get eq changed), "The outgoing leg must invalidate the route")
    }
    check("live flight commands reveal successive town halls and continue directly to the raid") {
      world(128, 96)
      val neutral = new PlayerInfo(null) { override lazy val isNeutral = true; override lazy val isAlly = false; override lazy val isUs = false; override lazy val isEnemy = false }
      val scouts = Vector(base(85, 4), base(60, 7), base(35, 4))
      scouts.foreach(_.owner = neutral)
      val target = base(5, 10)
      val start = Tile(116, 9).center
      val t = unit(Protoss.Shuttle, start.x, start.y); val r = unit(Protoss.Reaver, start.x, start.y)
      val m = new MissionProbe(scouts.head); m.autonomousLanding = true; m.scoutTargets = true
      m.configure(Vector(t), Vector(r)); m.itinerary ++= scouts.tail :+ target; load(r, Some(t)); m.run()
      var distance = 0.0
      var steps = 0
      while (m.itinerary.head != target && steps < 300) {
        scouts.flatMap(_.townHallArea.tiles).filter(p => p.center.pixelDistance(t.pixel) <= t.sightPixels - 24).foreach(p => vision.visible(p.x, p.y))
        With.frame += 6; t.sleepUntil = 0
        performIntent(t)
        val to = movement(t)
        val next = t.pixel.project(to, Math.min(32, t.pixelDistanceCenter(to)))
        distance += t.pixel.pixelDistance(next)
        set(t, "_pixel", next); set(t, "_tile", next.tile)
        set(r, "_pixel", next); set(r, "_tile", next.tile)
        steps += 1
      }
      assert(m.itinerary.head == target, s"Flight stalled at ${t.pixel} with ${m.itinerary.map(_.townHallTile)}")
      assert(scouts.forall(_.townHallArea.tiles.forall(_.visible)), "Every town hall footprint must be revealed")
      assert(distance < start.pixelDistance(t.pixel) + 160, s"Excessive scouting detour: $distance from $start to ${t.pixel}")
      assert(r.loaded && t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].unloaded == null)
    }
    check("scouting an empty town hall advances the flight without waiting for a mission update") {
      world(128)
      val empty = base(62, 4); val target = base(4, 62)
      empty.owner = new PlayerInfo(null) { override lazy val isNeutral = true; override lazy val isAlly = false; override lazy val isUs = false; override lazy val isEnemy = false }
      empty.lastFrameScoutedByUs = With.frame - 10
      val t = unit(Protoss.Shuttle, 2300, 180); val r = unit(Protoss.Reaver, 2300, 180)
      val m = new MissionProbe(empty); m.scoutTargets = true; m.autonomousLanding = true
      m.configure(Vector(t), Vector(r)); m.itinerary += target; load(r, Some(t)); m.run()
      assert(m.state == m.Travelling && m.itinerary.head == empty && r.loaded)
      empty.townHallArea.tiles.dropRight(1).foreach(p => vision.visible(p.x, p.y))
      performIntent(t)
      assert(m.itinerary.head == empty, "Partial town hall vision cannot skip an expansion")
      val unseen = empty.townHallArea.tiles.last; vision.visible(unseen.x, unseen.y)
      With.frame += 6; t.sleepUntil = 0; performIntent(t)
      assert(m.itinerary.head == target && m.vicinity == target.heart.center)
      assert(r.loaded && t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].unloaded == null)
      assert(t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].movedTo != null)
    }
    check("a newly observed Nexus prevents clearing a scouting stop before ownership updates") {
      world(128)
      val target = base(62, 4)
      target.owner = new PlayerInfo(null) { override lazy val isNeutral = true; override lazy val isAlly = false; override lazy val isUs = false; override lazy val isEnemy = false }
      val t = unit(Protoss.Shuttle, target.townHallArea.center.x + 160, target.townHallArea.center.y)
      val r = unit(Protoss.Reaver, t.pixel.x, t.pixel.y)
      val m = new MissionProbe(target); m.scoutTargets = true; m.autonomousLanding = true
      m.configure(Vector(t), Vector(r)); load(r, Some(t)); m.run()
      target.townHallArea.tiles.foreach(p => vision.visible(p.x, p.y))
      val nexus = unit(Protoss.Nexus, target.townHallArea.center.x, target.townHallArea.center.y)
      set(nexus, "_player", enemy); set(nexus, "_tileTopLeft", target.townHallTile)
      val previous = With.units.ours.toVector
      set(With.units, "bufferFriendly", new ProxyBwapi.UnitTracking.UnorderedBuffer(Vector(nexus)))
      m.run()
      assert(m.itinerary.head == target && m.state == m.Travelling && r.loaded)
      target.owner = enemy; target.units = Vector(nexus); m.run()
      assert(m.state == m.Landing && !m.controlsCombat(t) && t.intent.terminus.contains(target.heart.center),
        "Discovering ownership must finish the flight to the mineral line")
      Vector(t, r).foreach(u => { set(u, "_pixel", target.heart.center); set(u, "_tile", target.heart) })
      m.run()
      assert(m.state == m.Raiding && m.vicinity == target.heart.center && t.intent.terminus.contains(m.vicinity))
      assert(r.loaded && t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].unloaded == null)
      set(With.units, "bufferFriendly", new ProxyBwapi.UnitTracking.UnorderedBuffer(previous))
    }
    check("fresh anti-air invalidates a live transport command between mission updates") {
      world(128)
      val target = base(115, 5)
      val t = unit(Protoss.Shuttle, 336, 80); val r = unit(Protoss.Reaver, 336, 80)
      val m = new MissionProbe(target); m.autonomousLanding = true; m.configure(Vector(t), Vector(r)); load(r, Some(t)); m.run()
      performIntent(t)
      val original = movement(t)
      val hazard = t.pixel.project(original, 380).tile
      val turret = unit(ProxyBwapi.Races.Terran.MissileTurret, hazard.center.x, hazard.center.y + 128)
      set(turret, "_player", enemy)
      val previous = With.units.ours.toVector
      set(With.units, "bufferFriendly", new ProxyBwapi.UnitTracking.UnorderedBuffer(Vector(turret)))
      With.frame += 6; t.sleepUntil = 0; t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].movedTo = null
      performIntent(t)
      val changed = movement(t)
      assert(changed != original && Mathematics.Shapes.Ray(t.pixel, changed).forall(_.tile.enemyRangeAgainst(t) == 0))
      set(With.units, "bufferFriendly", new ProxyBwapi.UnitTracking.UnorderedBuffer(previous))
      assert(r.loaded && t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].unloaded == null)
    }
    check("a complete anti-air barrier fails closed instead of accepting a costly unsafe route") {
      world(128)
      With.grids.enemyRangeAir.tiles.foreach(t => t.maxDepth = if (t.tile.x == 64) 1 else 0)
      assert(new DropRoute().next(Tile(10, 10), Tile(110, 10)).isEmpty)
    }
    def defend(m: MissionProbe, t: FriendlyUnitInfo, r: FriendlyUnitInfo, count: Int, judgment: Option[Boolean], kind: UnitClass = Protoss.Dragoon): Unit = {
      val defenders = Vector.fill(count)(unit(kind, t.pixel.x + 128, t.pixel.y))
      defenders.foreach(d => {
        set(d, "_player", enemy)
        With.grids.enemyRangeAir.updateUnit(d); With.grids.enemyRangeAirGround.updateUnit(d)
      }); m.target.units ++= defenders
      t.matchups = new MatchupAnalysis(t) {
        override def enemies = defenders
        override def threats = defenders
        override def threatsInRange = defenders
        override def withinSafetyMargin = false
      }
      val battle = new Information.Battles.Types.Battle(Vector(t, r), defenders, isGlobal = false)
      battle.judgement = judgment.map(fight => {
        val j = allocate(classOf[Information.Battles.Types.BattleJudgment]); set(j, "shouldFightGround", fight); j
      })
      With.battles.byUnit += r -> battle
    }
    check("rejecting the final stop in micro reverses to an earlier eligible base") {
      world(128)
      val a = base(5, 7); val b = base(9, 41)
      set(With.geography, "_bases", Vector(a, b))
      val t = unit(Protoss.Shuttle, 288, 1120); val r = unit(Protoss.Reaver, 288, 1120)
      val m = new MissionProbe(b); m.continuePressure = true; m.autonomousLanding = true
      m.configure(Vector(t), Vector(r)); load(r, Some(t))
      m.itinerary.clear(); m.appendTargets(Vector(a, b)); m.itinerary.clear(); m.itinerary += b
      m.updateState(false); m.state = m.Travelling; m.launched = true
      t.intend(m).setAction(m.ActionTravelTransport).setTerminus(b.heart.center)
      defend(m, t, r, 12, None)
      performIntent(t)
      assert(m.launched && m.state == m.Travelling && m.itinerary.toVector == Vector(a))
      assert(t.intent.action == m.ActionTravelTransport && t.intent.terminus.contains(a.heart.center) && r.loaded)
      With.frame += 6; t.sleepUntil = 0; performIntent(t)
      assert(m.itinerary.toVector == Vector(a) && m.state == m.Travelling,
        "Threats at the previous stop cannot cancel the return visit from far away")
    }
    check("a large base region cannot reject a distant return approach") {
      world(128)
      val target = base(5, 7)
      val t = unit(Protoss.Shuttle, target.heart.center.x, target.heart.center.y + 620)
      val r = unit(Protoss.Reaver, t.pixel.x, t.pixel.y)
      val m = new MissionProbe(target); m.assessDefense = true; m.autonomousLanding = true
      m.configure(Vector(t), Vector(r)); load(r, Some(t)); m.updateState(false)
      defend(m, t, r, 12, None)
      target.units.filter(_.canAttack).foreach(d => {
        set(d, "_pixel", target.heart.center.add(128, 0)); set(d, "_tile", d.pixel.tile)
        With.grids.enemyRangeAir.updateUnit(d); With.grids.enemyRangeAirGround.updateUnit(d)
      })
      assert(t.base.contains(target) && !m.mustLeave && !m.controlsCombat(t))
      set(t, "_pixel", target.heart.center.add(0, 320)); set(t, "_tile", t.pixel.tile)
      assert(m.mustLeave, "Defense is evaluated when the crew actually approaches the destination")
    }
    check("evacuation rescues a nearby Reaver even when the Shuttle is under fire") {
      world(128)
      val target = base(9, 41)
      val t = unit(Protoss.Shuttle, 224, 1101); val r = unit(Protoss.Reaver, 251, 1148)
      val m = new MissionProbe(target); m.configure(Vector(t), Vector(r)); m.state = m.Evacuating
      val threat = unit(Protoss.Dragoon, 400, 1100); set(threat, "_player", enemy)
      t.matchups = new MatchupAnalysis(t) {
        override def threats = Vector(threat)
        override def enemies = Vector(threat)
        override def framesOfSafety = -1.0
        override def withinSafetyMargin = false
      }
      With.grids.enemyRangeAir.tiles.foreach(_.maxDepth = 1)
      assert(!Micro.Coordination.Pathing.MicroPathing.isSafeMove(t, r.pixel))
      perform(m.ActionEvacuateTransport, t)
      assert(t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].clicked == r.bwapiUnit)
      assert(t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].movedTo == null,
        "Nearby rescue cannot be overridden by a Shuttle retreat")
    }
    check("Gladiator's newly revealed third stays on the itinerary despite the pessimistic army forecast") {
      world(128)
      val target = base(118, 77)
      target.heart; set(target, "heart", Tile(123, 77))
      val t = unit(Protoss.Shuttle, 3712, 2760)
      val r = unit(Protoss.Reaver, t.x, t.y); val d = unit(Protoss.Dragoon, t.x, t.y)
      val drone = unit(ProxyBwapi.Races.Zerg.Drone, 3911, 2548)
      val defenders = Vector(unit(ProxyBwapi.Races.Zerg.Mutalisk, 3847, 2988)) ++
        Vector((3910, 2972), (3936, 2972), (3944, 2836), (3960, 2900)).map { case (x, y) => unit(ProxyBwapi.Races.Zerg.Zergling, x, y) }
      (defenders :+ drone).foreach(e => {
        set(e, "_player", enemy)
        With.grids.enemyRangeAir.updateUnit(e); With.grids.enemyRangeAirGround.updateUnit(e)
      })
      target.units = defenders :+ drone
      val m = new MissionProbe(target); m.autonomousLanding = true; m.assessDefense = true; m.launched = true
      m.configure(Vector(t), Vector(r, d)); load(r, Some(t)); load(d, Some(t))
      val battle = new Information.Battles.Types.Battle(Vector(t, r, d), defenders, isGlobal = false)
      val judgment = allocate(classOf[Information.Battles.Types.BattleJudgment]); set(judgment, "shouldFightGround", false)
      battle.judgement = Some(judgment); With.battles.byUnit += r -> battle
      m.run()
      assert(m.itinerary.headOption.contains(target) && m.state == m.Landing && !m.mustLeave && r.loaded)
      set(t, "_pixel", target.heart.center); set(t, "_tile", target.heart)
      m.run()
      assert(m.state == m.Raiding && m.controlsCombat(t), "The mineral line must receive the crew once reached")
    }
    check("a weaker local defense permits a raid even without an optimistic forecast") {
      for (judgment <- Vector(None, Some(false), Some(true))) {
        val (m, t, r, z, _) = combatMission(approaching = true)
        defend(m, t, r, 1, judgment)
        m.run()
        assert(m.state == m.Raiding && Vector(t, r, z).forall(_.intent.action == Idle), s"Weaker defense became ${m.state} with $judgment")
        assert(r.loaded && z.loaded && t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].unloaded == null)
      }
    }
    check("raids use the drop forecast instead of the shared army judgment") {
      for (fight <- Vector(false, true)) {
        val (m, t, r, z, _) = combatMission(approaching = true)
        defend(m, t, r, 12, Some(!fight), Protoss.Zealot)
        m.launched = true
        val original = With.battles.byUnit(r)
        val army = unit(Protoss.Dragoon, t.pixel.x, t.pixel.y)
        val physical = new Information.Battles.Types.Battle(Vector(t, r, z, army), original.enemy.units, isGlobal = false)
        physical.judgement = original.judgement
        val drop = physical.combatForecastByUnit(r)
        assert((drop ne physical) && !drop.us.units.contains(army))
        val judgment = allocate(classOf[Information.Battles.Types.BattleJudgment])
        set(judgment, "shouldFightGround", fight)
        drop.judgement = Some(judgment)
        With.battles.byUnit += r -> physical
        m.run()
        assert(m.state == (if (fight) m.Raiding else m.Escaping))
      }
    }
    check("an optimistic drop forecast permits stronger ground defense") {
      val (m, t, r, z, _) = combatMission(approaching = true)
      defend(m, t, r, 12, Some(true), Protoss.Zealot)
      m.run()
      assert(m.state == m.Raiding && m.controlsCombat(t) && r.loaded && z.loaded)
    }
    check("an optimistic ground forecast cannot override lethal air exposure") {
      val (m, t, r, z, _) = combatMission(approaching = true)
      defend(m, t, r, 12, Some(true))
      m.run()
      assert(m.state == m.Escaping && !m.controlsCombat(t) && r.loaded && z.loaded)
    }
    check("defense discovered between mission updates abandons the base without unloading") {
      val (m, t, r, z, _) = combatMission(approaching = true)
      Vector(t, r, z).foreach(u => { set(u, "_pixel", m.target.heart.center.add(-700, 0)); set(u, "_tile", u.pixel.tile) })
      m.run(); assert(!m.controlsCombat(t))
      Vector(t, r, z).foreach(u => { set(u, "_pixel", m.target.heart.center.add(-320, 0)); set(u, "_tile", u.pixel.tile) })
      defend(m, t, r, 12, Some(false), Protoss.Zealot)
      With.frame += 6; t.sleepUntil = 0; performIntent(t)
      assert(m.state == m.Escaping && !m.controlsCombat(t) && r.loaded && z.loaded)
      assert(t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].unloaded == null)
    }
    check("local strength uses the remaining health of both armies") {
      val (m, t, r, z, _) = combatMission(approaching = true)
      defend(m, t, r, 4, Some(false), Protoss.Zealot)
      m.run(); assert(m.state == m.Raiding)
      set(r, "_hitPoints", r.unitClass.maxHitPoints / 2); set(r, "_shieldPoints", r.unitClass.maxShields / 2)
      assert(m.mustLeave, "A damaged crew cannot count as full-strength")
      m.target.units.filter(Protoss.Zealot).foreach(d => {
        set(d, "_hitPoints", d.unitClass.maxHitPoints / 2); set(d, "_shieldPoints", d.unitClass.maxShields / 2)
      })
      assert(!m.mustLeave, "Damaged defenders count at their remaining strength too")
    }
    check("landed troops still evaluate local defense when their Shuttle leaves the approach radius") {
      val (m, t, r, z, _) = combatMission()
      defend(m, t, r, 4, Some(false), Protoss.Zealot)
      m.run(); assert(m.state == m.Raiding)
      load(r, None)
      set(t, "_pixel", m.target.heart.center.add(-600, 0)); set(t, "_tile", t.pixel.tile)
      set(r, "_hitPoints", 20); set(r, "_shieldPoints", 0)
      assert(m.mustLeave, "Grounded troops cannot lose the defense check when the Shuttle moves away")
    }
    check("a large distant army and mineral-line workers do not veto a local raid") {
      val (m, t, r, z, _) = combatMission(approaching = true)
      defend(m, t, r, 12, Some(false))
      m.target.units.filter(_.canAttack).foreach(d => {
        set(d, "_pixel", m.target.heart.center.add(900, 0)); set(d, "_tile", d.pixel.tile)
        With.grids.enemyRangeAir.updateUnit(d); With.grids.enemyRangeAirGround.updateUnit(d)
      })
      val workers = Vector.fill(20)(unit(ProxyBwapi.Races.Zerg.Drone, m.target.heart.center.x, m.target.heart.center.y))
      workers.foreach(set(_, "_player", enemy)); m.target.units ++= workers
      Vector(t, r, z).foreach(u => { set(u, "_pixel", m.target.heart.center); set(u, "_tile", u.pixel.tile) })
      m.run()
      assert(m.state == m.Raiding && !m.mustLeave)
    }
    check("anti-air exposure can veto a weaker defense and tightens as Shuttle health falls") {
      val (m, t, r, z, _) = combatMission(approaching = true)
      defend(m, t, r, 1, Some(false))
      m.run(); assert(m.state == m.Raiding)
      set(t, "_hitPoints", 20); set(t, "_shieldPoints", 0)
      assert(m.mustLeave, "The flight budget must use remaining Shuttle health")
    }
    check("anti-air outside the flight corridor does not spend the Shuttle health budget") {
      val (m, t, r, z, _) = combatMission(approaching = true)
      defend(m, t, r, 2, Some(false), ProxyBwapi.Races.Terran.MissileTurret)
      m.target.units.filter(_.canAttack).foreach(d => {
        set(d, "_pixel", m.target.heart.center.add(32, 300)); set(d, "_tile", d.pixel.tile)
        With.grids.enemyRangeAir.updateUnit(d); With.grids.enemyRangeAirGround.updateUnit(d)
        assert(d.pixelDistanceEdge(m.target.heart.center) > d.pixelRangeAir + t.unitClass.dimensionMax)
      })
      m.run()
      assert(m.state == m.Raiding && !m.mustLeave, "Off-route anti-air cannot veto a harmless flight")
    }
    check("flight risk charges at least one full hit from an interceptor") {
      val (m, t, r, z, _) = combatMission(approaching = true)
      defend(m, t, r, 1, Some(false), ProxyBwapi.Races.Zerg.Scourge)
      m.run()
      assert(m.state == m.Escaping && !m.controlsCombat(t), "A 110-damage interception exceeds half a healthy Shuttle's health")
    }
    check("air attackers approaching during deployment count before entering weapon range") {
      val (m, t, r, z, _) = combatMission(approaching = true)
      defend(m, t, r, 1, Some(true), ProxyBwapi.Races.Zerg.Scourge)
      val scourge = m.target.units.find(ProxyBwapi.Races.Zerg.Scourge).get
      set(scourge, "_pixel", m.target.heart.center.add(0, 240)); set(scourge, "_tile", scourge.pixel.tile)
      With.grids.enemyRangeAir.updateUnit(scourge); With.grids.enemyRangeAirGround.updateUnit(scourge)
      assert(scourge.pixelDistanceEdge(m.target.heart.center) > scourge.pixelRangeAir + t.unitClass.dimensionMax)
      m.run()
      assert(m.state == m.Escaping && !m.controlsCombat(t) && r.loaded,
        "A Scourge that can arrive during unloading must not be treated as stationary")
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
    check("a fighting grounded Reaver requests both Zealots through normal Shuttle unloading") {
      val (m, t, r, z, _) = combatMission()
      val other = unit(Protoss.Zealot, t.pixel.x, t.pixel.y)
      m.passengers += other; other.setSquad(Some(m)); t.agent.addPassenger(other); load(other, Some(t))
      m.run(); load(r, None); r.agent.shouldFight = true
      for (passenger <- Vector(z, other)) {
        passenger.agent.wantsUnload = false // Passenger micro clears transient requests every update.
        t.agent.run = t.agent.passengers.indexOf(passenger); t.sleepUntil = With.frame + 10
        perform(Transport, t)
        assert(t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].unloaded == passenger.bwapiUnit,
          "The mission must request each Zealot without requiring its own attack or delivery goal")
        load(passenger, None)
      }
      assert(t.intent.action == Idle && Vector(r, z, other).forall(_.intent.action == Idle))
    }
    check("mission Zealot unloading stops when the Reaver boards, retreats, or the mission recovers") {
      val (m, t, r, z, _) = combatMission(); m.run()
      def unloadAttempt(): Boolean = {
        t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].unloaded = null
        z.agent.wantsUnload = false; t.agent.run = t.agent.passengers.indexOf(z); t.sleepUntil = With.frame + 10
        perform(Transport, t)
        t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].unloaded == z.bwapiUnit
      }
      r.agent.shouldFight = true
      assert(!unloadAttempt(), "A loaded Reaver does not deploy its escorts")
      load(r, None); r.agent.shouldFight = false
      assert(!unloadAttempt(), "A retreating Reaver does not deploy its escorts")
      r.agent.shouldFight = true
      assert(unloadAttempt())
      load(r, Some(t)); assert(!unloadAttempt(), "A previous mission request cannot survive boarding")
      load(r, None)
      for (phase <- Vector(m.Assembling, m.Travelling, m.Evacuating, m.Escaping)) {
        m.state = phase
        assert(!unloadAttempt(), s"No escort deployment during $phase")
      }
    }
    check("a landed crew deploys its Zealot while another Shuttle keeps its own crew aboard") {
      val (m, t, r, z, _) = combatMission()
      val other = unit(Protoss.Shuttle, 1800, 3280)
      val otherReaver = unit(Protoss.Reaver, 1800, 3280); val otherZealot = unit(Protoss.Zealot, 1800, 3280)
      m.transports += other; m.passengers ++= Vector(otherReaver, otherZealot)
      Vector(other, otherReaver, otherZealot).foreach(_.setSquad(Some(m)))
      Vector(otherReaver, otherZealot).foreach(p => { other.agent.addPassenger(p); load(p, Some(other)) })
      m.run(); load(r, None); r.agent.shouldFight = true
      assert(m.state == m.Landing && m.controlsCombat(t) && !m.controlsCombat(other))
      for (phase <- Vector(m.Landing, m.Raiding)) {
        m.state = phase
        Vector(t -> z, other -> otherZealot).foreach { case (shuttle, zealot) =>
          shuttle.agent.run = shuttle.agent.passengers.indexOf(zealot); shuttle.sleepUntil = With.frame + 10
          perform(Transport, shuttle)
          assert((shuttle.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].unloaded == zealot.bwapiUnit) == (shuttle == t))
        }
      }
    }
    check("default Shuttle combat honors a loaded Zealot ride goal beside a grounded Reaver") {
      val (m, t, r, z, worker) = combatMission(); m.run(); load(r, None)
      val previousUnits = With.units.ours.toVector
      set(With.units, "bufferFriendly", new ProxyBwapi.UnitTracking.UnorderedBuffer(Vector(t, r, z)))
      Vector(t, r, z).foreach(u => u.matchups = new MatchupAnalysis(u) {
        override def threats = Vector.empty
        override def threatsInRange = Vector.empty
        override def enemies = Vector.empty
        override def pixelsToThreatRange = None
      })
      val goal = t.pixel.add(120, 0)
      z.agent.toAttack = Some(worker); z.agent.setRideGoal(goal)
      perform(Micro.Actions.Protoss.Shuttle.BeShuttle, t)
      assert(t.agent.decision.pixel.contains(goal), "The Zealot's delivery request must remain eligible and unclamped")
      val moved = t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].movedTo
      assert(moved != null && moved.getX > t.pixel.x + 120, "Ordinary delivery retains acceleration overshoot")
      set(With.units, "bufferFriendly", new ProxyBwapi.UnitTracking.UnorderedBuffer(previousUnits))
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
      assert(other.intent.action == m.ActionTravelTransport && otherReaver.intent.action == Micro.Actions.Basic.DoNothing)
      assert(other.intent.station.isEmpty && other.intent.terminus.contains(m.approach(other)))
      set(other, "_pixel", t.pixel); set(other, "_tile", t.tile)
      set(otherReaver, "_pixel", t.pixel); set(otherReaver, "_tile", t.tile)
      m.run()
      assert(m.state == m.Raiding && Vector(other, otherReaver).forall(_.intent.action == Idle))
      assert(other.intent.station.isEmpty, "Combat must not inherit the delivery waypoint")
    }
    check("an empty Shuttle returning home cannot cancel the other crews' approach") {
      val (m, _, _, _, _) = combatMission()
      val approaching = unit(Protoss.Shuttle, 1800, 3280)
      val r = unit(Protoss.Reaver, 1800, 3280)
      val empty = unit(Protoss.Shuttle, 3900, 2800)
      m.transports ++= Vector(approaching, empty); m.passengers += r
      Vector(approaching, empty, r).foreach(_.setSquad(Some(m))); load(r, Some(approaching))
      m.run(); assert(m.state == m.Landing && !m.controlsCombat(approaching))
      performIntent(empty)
      for (_ <- 1 to 2) { With.frame += Seconds(8)(); empty.sleepUntil = 0; performIntent(empty) }
      assert(m.itinerary.headOption.contains(m.target) && m.state == m.Landing && r.loaded)
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
    check("losing the last Reaver retains the empty Shuttle for routed withdrawal") {
      val (m, t, r, z, _) = combatMission(); m.run()
      set(r, "_alive", false); set(z, "_alive", false)
      m.run()
      assert(m.launched && m.passengers.isEmpty && m.state == m.Escaping)
      assert(t.intent.action == m.ActionTravelTransport && m.transports.contains(t))
    }
    check("assembly keeps both Zealots until the entire crew has boarded") {
      val (m, t, r, z, _) = combatMission(); load(z, None)
      val secondZealot = unit(Protoss.Zealot, t.pixel.x, t.pixel.y)
      m.passengers += secondZealot; secondZealot.setSquad(Some(m))
      m.run(); assert(m.state == m.Assembling)
      With.frame += Seconds(4)(); load(z, Some(t)); m.run()
      With.frame += Seconds(4)(); m.run()
      assert(m.state == m.Assembling && m.passengers.contains(secondZealot))
      assert(m.assignedTransport(secondZealot).contains(t) && secondZealot.agent.ride.contains(t))
      load(secondZealot, Some(t)); m.run()
      assert(m.state == m.Raiding && Vector(r, z, secondZealot).forall(_.loaded))
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
      assert(m.passengers.contains(z) && m.state == m.Evacuating)
      load(z, Some(t)); m.run()
      assert(m.state == m.Escaping)
    }
    check("mission pickup approaches are planned as intentions for assembly and recovery") {
      for (recovering <- Vector(false, true)) {
        world(128)
        val target = base(110)
        val t = unit(Protoss.Shuttle, 160, 160)
        val r = unit(Protoss.Reaver, 760, 160)
        val m = new MissionProbe(target); m.configure(Vector(t), Vector(r))
        if (recovering) {
          load(r, Some(t)); m.run(); load(r, None); m.itinerary.clear()
        }
        m.run()
        assert(m.state == (if (recovering) m.Evacuating else m.Assembling))
        assert(t.intent.action == m.ActionTravelTransport && t.intent.station.isEmpty && t.intent.terminus.contains(r.tile.center))
        assert(t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].movedTo == null, "Planning must not issue movement commands")
        performIntent(t)
        assert(t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].movedTo != null)
        assert(t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].clicked == null)
        set(t, "_pixel", r.pixel.add(-100, 0)); set(t, "_tile", t.pixel.tile)
        m.run()
        assert(t.intent.station.isEmpty)
        assert(t.intent.action == (if (recovering) m.ActionEvacuateTransport else m.ActionAssembleTransport))
      }
    }
    check("a stalled mission clears the flight waypoint and holds without nearby threats") {
      world(128)
      val t = unit(Protoss.Shuttle, 160, 160)
      val r = unit(Protoss.Reaver, 760, 160)
      val m = new MissionProbe(base(110)); m.configure(Vector(t), Vector(r)); m.run()
      assert(t.intent.action == m.ActionTravelTransport && t.intent.station.isEmpty)
      performIntent(t)
      t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].movedTo = null
      With.frame += Seconds(8)(); t.sleepUntil = 0
      performIntent(t)
      assert(t.unready && t.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].movedTo == null)
    }
    def turretEscape(): (FriendlyUnitInfo, FriendlyUnitInfo) = {
      world(128)
      val t = unit(Protoss.Shuttle, 3969, 1097)
      set(t, "_angleRadians", Math.PI / 2)
      val turret = unit(ProxyBwapi.Races.Terran.MissileTurret, 3712, 1152)
      set(turret, "_player", enemy)
      With.grids.enemyRangeAir.updateUnit(turret)
      set(t.agent, "_defaultHome", new Cache(() => Pixel(464, 448)))
      t.agent.home.set(Pixel(464, 448))
      t.matchups = new MatchupAnalysis(t) {
        override def threats = Vector(turret)
        override def enemies = Vector(turret)
        override def threatsInRange = threats.filter(_.inRangeToAttack(t))
        override def pixelsEntangled = turret.pixelRangeAgainst(t) - turret.pixelDistanceEdge(t)
        override def withinSafetyMargin = pixelsEntangled <= -64
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
      assert(distance > 0, s"Escape must move after clipping to the map: $to")
      (0 to 64).foreach(i => {
        val at = t.pixel.project(to, distance * i / 64.0)
        assert(threat.pixelDistanceEdge(t, at) >= Math.min(before, threat.pixelRangeAgainst(t) + margin) - 1,
          s"Escape crosses the threat at $at toward $to")
      })
      assert(threat.pixelDistanceEdge(t, to) >= threat.pixelRangeAgainst(t) + margin, s"Escape ends in weapon range at $to")
    }
    check("a Shuttle retreat escapes the turret before turning toward home") {
      val (t, turret) = turretEscape()
      Micro.Actions.Combat.Maneuvering.Retreat(t)
      avoidsThreat(t, turret, movement(t))
    }
    check("an empty Shuttle returns to normal retreat after losing its crew") {
      val (t, turret) = turretEscape()
      val m = new MissionProbe(base(5)); m.autonomousLanding = true; m.launched = true
      set(With.geography, "_baseByTile", Array.fill[Option[Base]](With.mapTileArea)(None))
      m.configure(Vector(t), Vector.empty); m.run()
      assert(!m.launched && t.intent.action == Idle && t.squad.isEmpty)
      set(t.agent, "_defaultHome", new Cache(() => Pixel(464, 448)))
      Retreat(t)
      avoidsThreat(t, turret, movement(t))
    }
    check("post-pickup mission travel clears anti-air before following the next target") {
      val (t, turret) = turretEscape()
      val target = base(5)
      val r = unit(Protoss.Reaver, t.pixel.x, t.pixel.y)
      val m = new MissionProbe(target); m.configure(Vector(t), Vector(r))
      set(With.geography, "_baseByTile", Array.fill[Option[Base]](With.mapTileArea)(None))
      t.agent.addPassenger(r); load(r, Some(t)); m.updateState(false)
      m.run(); performIntent(t)
      avoidsThreat(t, turret, movement(t))
    }
    check("post-pickup movement reduces Marine threat depth and reacts to a new blocker") {
      world(128)
      val t = unit(Protoss.Shuttle, 1372, 155)
      set(t.agent, "_defaultHome", new Cache(() => Pixel(464, 448)))
      set(t, "_angleRadians", 175 * 2 * Math.PI / 256 - Math.PI / 2)
      val marines = Vector((1329, 287), (1421, 138), (1349, 267), (1370, 284))
        .map { case (x, y) => unit(ProxyBwapi.Races.Terran.Marine, x, y) }
      marines.foreach(set(_, "_player", enemy))
      marines.foreach(With.grids.enemyRangeAir.updateUnit)
      t.matchups = new MatchupAnalysis(t) {
        override def threats = marines
        override def enemies = threats
        override def withinSafetyMargin = false
        override def threatsInRange = threats.filter(_.inRangeToAttack(t))
      }
      val r = unit(Protoss.Reaver, t.pixel.x, t.pixel.y)
      val m = new MissionProbe(base(5)); m.configure(Vector(t), Vector(r))
      set(With.geography, "_baseByTile", Array.fill[Option[Base]](With.mapTileArea)(None))
      t.agent.addPassenger(r); load(r, Some(t)); m.updateState(false)
      m.run(); performIntent(t)
      assert(movement(t).tile.enemyRangeAgainst(t) < t.tile.enemyRangeAgainst(t), s"Retreat ${t.pixel} -> ${movement(t)} must reduce threat depth")
      marines.foreach(marine => assert(marine.pixelDistanceEdge(t, movement(t)) > marine.pixelRangeAgainst(t)))
      // A newly revealed Marine blocks the original westbound flight path.
      t.sleepUntil = 0; set(t, "_pixel", Pixel(1310, 148)); set(t, "_tile", t.pixel.tile)
      val western = unit(ProxyBwapi.Races.Terran.Marine, 1071, 152); set(western, "_player", enemy)
      With.grids.enemyRangeAir.updateUnit(western)
      val all = marines :+ western
      t.matchups = new MatchupAnalysis(t) {
        override def threats = all
        override def enemies = threats
        override def withinSafetyMargin = false
        override def threatsInRange = threats.filter(_.inRangeToAttack(t))
      }
      m.run(); performIntent(t)
      assert(movement(t).tile.enemyRangeAgainst(t) < t.tile.enemyRangeAgainst(t), s"Retreat ${t.pixel} -> ${movement(t)} must reduce threat depth")
      assert(Mathematics.Shapes.Ray(t.pixel, movement(t)).forall(_.tile.enemyRangeAgainst(t) <= t.tile.enemyRangeAgainst(t)),
        "The full retreat command, including overshoot, must not cross deeper anti-air")
    }
    check("nearby rescue and combat unloading can interrupt a previous Shuttle escape") {
      val (t, _) = turretEscape()
      val r = unit(Protoss.Reaver, t.pixel.x - 80, t.pixel.y)
      t.agent.addPassenger(r)
      Micro.Actions.Combat.Maneuvering.Retreat(t)
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
      r.agent.wantsPickup = true
      r.matchups = new MatchupAnalysis(r) {
        override def enemies = Vector.empty
        override def threatsInRange = Vector.empty
        override def pixelsToThreatRange = Some(0.0)
      }
      perform(Micro.Actions.Protoss.Shuttle.BeShuttle, t)
      avoidsThreat(t, turret, movement(t))
      set(t, "_pixel", movement(t)); set(t, "_tile", t.pixel.tile)
      r.sleepUntil = 0; set(r, "_order", ProxyBwapi.Orders.EnterTransport)
      Micro.Agency.Commander.rightClick(r, t)
      assert(r.bwapiUnit.asInstanceOf[bwapi.DropTestUnit].stopped,
        "A distant EnterTransport order must stay blocked after leaving immediate danger")
      var steps = 0
      while (t.pixelDistanceCenter(r) > 384 && steps < 64) {
        t.sleepUntil = 0
        t.agent.decision.clear()
        perform(Micro.Actions.Protoss.Shuttle.BeShuttle, t)
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
