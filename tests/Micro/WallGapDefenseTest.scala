package bwapi {
  class WorkerRecruitmentPlayer extends Player(null, 0, null) {
    override def isAlly(player: Player): Boolean = false
    override def isEnemy(player: Player): Boolean = false
    override def isNeutral: Boolean = false
  }
  class WallGapTestUnit extends Unit(null, 0, null) {
    var movedTo: Position = _
    var holds: Int = 0
    var stops: Int = 0
    override def getRemainingBuildTime: Int = 0
    override def move(to: Position): Boolean = { movedTo = to; true }
    override def stop(): Boolean = { stops += 1; true }
    override def holdPosition(): Boolean = { holds += 1; true }
  }
}

package Micro.Formation {
  import Information.Geography.Geography
  import Information.Geography.Types.{Base, Zone}
  import Lifecycle.With
  import Lifecycle.Configure.Configuration
  import Mathematics.Points.{Pixel, Point, Tile}
  import Micro.Actions.Idle
  import Micro.Agency.Intention
  import Performance.{Cache, Latency}
  import Placement.Placement
  import Placement.Access.PlaceLabels
  import Placement.Generation.Fit
  import Placement.Templating.{RequireWalkable, Template, TemplatePointRequirement}
  import ProxyBwapi.ProxyBWAPI
  import ProxyBwapi.Players.PlayerInfo
  import ProxyBwapi.Races.Protoss
  import ProxyBwapi.UnitClasses.{UnitClass, UnitTypes}
  import ProxyBwapi.UnitInfo.FriendlyUnitInfo
  import ProxyBwapi.UnitTracking.{UnitTracker, UnorderedBuffer}
  import Tactic.Squads.SquadDefendBase
  import bwapi.Race

  object WallGapDefenseTest extends App {
    val unsafeField = classOf[sun.misc.Unsafe].getDeclaredField("theUnsafe")
    unsafeField.setAccessible(true)
    val unsafe = unsafeField.get(null).asInstanceOf[sun.misc.Unsafe]
    def allocate[T](clazz: Class[T]): T = unsafe.allocateInstance(clazz).asInstanceOf[T]
    def set(obj: AnyRef, name: String, value: Any): Unit = {
      val fields = Iterator.iterate[Class[_]](obj.getClass)(_.getSuperclass).takeWhile(_ != null).flatMap(_.getDeclaredFields)
      val matching = fields.filter(f => f.getName == name || f.getName.endsWith("$$" + name)).toVector
      assert(matching.nonEmpty, name)
      matching.foreach(f => { f.setAccessible(true); f.set(obj, value.asInstanceOf[AnyRef]) })
    }
    var passed = 0
    def check(name: String)(test: => Unit): Unit = { test; passed += 1; println("PASS " + name) }
    With.frame = 100
    With.mapTileWidth = 64; With.mapTileHeight = 64; With.mapTileArea = 64 * 64
    With.mapPixelWidth = 2048; With.mapPixelHeight = 2048
    With.proxy = allocate(classOf[ProxyBWAPI])
    set(With.proxy, "unitClasses", UnitTypes.all.map(UnitClass))
    With.game = allocate(classOf[bwapi.Game])
    set(With.game, "mapWidth", 64); set(With.game, "mapHeight", 64)
    set(With.game, "mapPixelWidth", 2048); set(With.game, "mapPixelHeight", 2048)
    set(With.game, "walkable", Array.fill(256, 256)(true))
    With.units = new UnitTracker
    With.configuration = new Configuration
    With.latency = allocate(classOf[Latency])
    With.geography = allocate(classOf[Geography])
    val zone = allocate(classOf[Zone])
    set(With.geography, "_zoneByTile", Array.fill(With.mapTileArea)(zone))
    val base = new Base("Natural", Tile(12, 20), Set.empty)
    val main = new Base("Main", Tile(12, 40), Set.empty)
    base.naturalOf = Some(main)
    set(With.geography, "_baseByTile", Array.fill[Option[Base]](With.mapTileArea)(Some(base)))
    def player(race: Race): PlayerInfo = {
      val p = allocate(classOf[PlayerInfo])
      set(p, "raceCurrentCache", new Cache(() => race))
      p
    }
    val zerg = player(Race.Zerg)
    val terran = player(Race.Terran)
    With.enemies = Vector(zerg)
    def unit(id: Int, kind: UnitClass, tile: Tile): FriendlyUnitInfo = {
      val u = allocate(classOf[FriendlyUnitInfo])
      set(u, "id", id); set(u, "hashCode", id)
      set(u, "_unitClass", kind)
      set(u, "_tileTopLeft", tile); set(u, "_tile", tile)
      set(u, "_pixel", tile.topLeftPixel.add(kind.tileWidth * 16, kind.tileHeight * 16))
      set(u, "_alive", true); set(u, "_complete", true)
      set(u, "bwapiUnit", allocate(classOf[bwapi.WallGapTestUnit]))
      u
    }
    val gateway = unit(1, Protoss.Gateway, Tile(10, 10))
    val forge = unit(2, Protoss.Forge, Tile(15, 10))
    val zealot = unit(3, Protoss.Zealot, Tile(14, 17))
    val probe = unit(4, Protoss.Probe, Tile(14, 18))
    val dragoon = unit(5, Protoss.Dragoon, Tile(14, 19))
    val defenders = Vector(probe, dragoon, zealot)
    def buildings(units: FriendlyUnitInfo*): Unit = set(With.units, "bufferFriendly", new UnorderedBuffer[FriendlyUnitInfo](units))
    def placement(label: Boolean = true): Unit = {
      With.placement = new Placement
      Vector(gateway, forge).foreach { u =>
        val requirement = new TemplatePointRequirement(u.unitClass.tileWidth, u.unitClass.tileHeight)
        if (label) requirement.withLabels(PlaceLabels.Wall)
        With.placement.index(Fit(u.tileTopLeft, new Template().add(Point(0, 0), requirement)))
      }
      With.placement.index(Fit(Tile(14, 10), new Template().add(Point(0, 0), new RequireWalkable)))
    }
    buildings(gateway, forge)
    placement()
    val positions = new WallGapPositions(base)
    check("occupied Wall-labelled 4x3 and 3x2 placements enable defense without a Wall object") {
      val result = positions(defenders)
      assert(result.keySet == Set(zealot, probe))
      assert(result(zealot).y < result(probe).y)
    }
    check("a Zerg enemy is required, including in a mixed matchup") {
      With.enemies = Vector(terran)
      assert(positions(defenders).isEmpty)
      With.enemies = Vector(terran, zerg)
      assert(positions(defenders).size == 2)
      With.enemies = Vector(zerg)
    }
    check("only natural bases activate the mode") {
      assert(new WallGapPositions(main)(defenders).isEmpty)
    }
    check("planned, missing, dead or unlabelled wall buildings do not enable the mode") {
      buildings(gateway)
      assert(positions(defenders).isEmpty)
      buildings(gateway, forge)
      set(forge, "_alive", false)
      assert(positions(defenders).isEmpty)
      set(forge, "_alive", true)
      placement(label = false)
      assert(positions(defenders).isEmpty)
      placement()
      assert(positions(defenders).size == 2)
    }
    check("incomplete buildings still collide, but incomplete or loaded defenders are not assigned") {
      set(forge, "_complete", false)
      assert(positions(defenders).size == 2)
      set(zealot, "_loaded", true)
      set(probe, "_complete", false)
      assert(positions(defenders).isEmpty)
      set(forge, "_complete", true); set(zealot, "_loaded", false); set(probe, "_complete", true)
    }
    check("a newly constructed blocker invalidates cached geometry immediately") {
      val before = positions(defenders)
      val blocker = unit(6, Protoss.PhotonCannon, Tile(14, 10))
      set(blocker, "_pixel", before(zealot))
      buildings(gateway, forge, blocker)
      val after = positions(defenders)
      assert(after != before)
      val obstacle = WallGapPositions.Box(blocker.left, blocker.top, blocker.rightExclusive, blocker.bottomExclusive)
      after.foreach { case (u, p) =>
        val kind = u.unitClass
        assert(! WallGapPositions.Body(kind.dimensionLeft, kind.dimensionUp, kind.dimensionRightExclusive, kind.dimensionDownExclusive).at(p).intersects(obstacle))
      }
      buildings(gateway, forge)
      assert(positions(defenders) == before)
    }
    check("support pylons in the wall template do not create rear wall entrances") {
      val before = positions(defenders)
      val pylon = unit(7, Protoss.Pylon, Tile(19, 14))
      With.placement.index(Fit(pylon.tileTopLeft, new Template().add(Point(0, 0),
        new TemplatePointRequirement(Protoss.Pylon).withLabels(PlaceLabels.Wall))))
      buildings(gateway, forge, pylon)
      assert(positions(defenders) == before)
      buildings(gateway, forge)
      placement()
    }
    check("wall slots outside the base tile region still defend its zone") {
      val baseTiles = Array.fill[Option[Base]](With.mapTileArea)(Some(base))
      baseTiles(gateway.tileTopLeft.i) = None
      set(With.geography, "_baseByTile", baseTiles)
      placement()
      assert(positions(defenders).size == 2)
      set(With.geography, "_baseByTile", Array.fill[Option[Base]](With.mapTileArea)(Some(base)))
      placement()
    }
    check("unlinked wall foundations may straddle the natural zone, but must overlap it") {
      val otherZone = allocate(classOf[Zone])
      val zones = Array.fill(With.mapTileArea)(zone)
      zones(gateway.tileTopLeft.i) = otherZone
      set(With.geography, "_zoneByTile", zones)
      placement()
      assert(positions(defenders).size == 2)
      for (x <- 0 until 4; y <- 0 until 3) zones(gateway.tileTopLeft.add(x, y).i) = otherZone
      assert(positions(defenders).isEmpty)
      set(With.geography, "_zoneByTile", Array.fill(With.mapTileArea)(zone))
      placement()
    }
    check("a wall outside the natural zone belongs to the base reached by its hallway") {
      val otherZone = allocate(classOf[Zone])
      val zones = Array.fill(With.mapTileArea)(zone)
      Vector(gateway, forge).foreach(u =>
        for (x <- 0 until u.unitClass.tileWidth; y <- 0 until u.unitClass.tileHeight) zones(u.tileTopLeft.add(x, y).i) = otherZone)
      set(With.geography, "_zoneByTile", zones)
      placement()
      val wall = new _root_.Placement.Walls.Wall
      wall.buildings ++= Vector(gateway.tileTopLeft -> Protoss.Gateway, forge.tileTopLeft -> Protoss.Forge)
      wall.hallway = Vector(base.townHallTile)
      With.placement.restoreCachedWall(Some(wall))
      assert(positions(defenders).size == 2)
      wall.hallway = Vector(gateway.tileTopLeft)
      assert(positions(defenders).isEmpty)
      set(With.geography, "_zoneByTile", Array.fill(With.mapTileArea)(zone))
      placement()
    }
    check("wall gap defense issues placement intents without commanding units") {
      val squad = allocate(classOf[SquadDefendBase])
      positions(defenders).foreach { case (defender, target) =>
        val api = defender.bwapiUnit.asInstanceOf[bwapi.WallGapTestUnit]
        Vector(target.add(40, 40), target).foreach { position =>
          set(defender, "_pixel", position)
          set(defender, "_intent", new Intention)
          squad.holdWallGap(defender, target)
          assert(defender.client == squad)
          assert(defender.intent.terminus.contains(target))
          assert(defender.intent.station.contains(target))
          assert(defender.intent.redoubt.contains(target))
          assert(! defender.intent.canFlee)
          assert(defender.intent.canFight)
          assert(defender.intent.action == Idle)
          assert(api.movedTo == null && api.holds == 0 && api.stops == 0)
        }
      }
    }
    check("a built natural wall anchors only defense squads in its own metro") {
      val remote = new Base("Expansion", Tile(48, 6), Set.empty)
      val homeBases = Vector(main, base)
      val homeMetro = new Information.Geography.Types.Metro(homeBases) {
        override lazy val bases = homeBases
      }
      val remoteMetro = new Information.Geography.Types.Metro(Vector(remote)) {
        override lazy val bases = Vector(remote)
      }
      set(With.geography, "_metros", Vector(homeMetro, remoteMetro))
      set(With.geography, "ourMainCache", new Cache(() => main))
      set(With.geography, "ourFoyerCache", new Cache(() => base))
      val cannon = unit(8, Protoss.PhotonCannon, Tile(16, 38))
      val wall = new _root_.Placement.Walls.Wall
      wall.buildings += cannon.tileTopLeft -> Protoss.PhotonCannon
      wall.gap = Some(Tile(21, 40))
      With.placement.restoreCachedWall(Some(wall))
      buildings(cannon)
      val mainDefense = new SquadDefendBase(main)
      val naturalDefense = new SquadDefendBase(base)
      val expansionDefense = new SquadDefendBase(remote)
      assert(mainDefense.plugWall().contains(wall.defensePoint))
      assert(naturalDefense.plugWall().contains(wall.defensePoint))
      assert(expansionDefense.plugWall().isEmpty)
      expansionDefense.run()
      assert(expansionDefense.vicinity == remote.heart.center)
      buildings()
      With.frame += 1
      assert(mainDefense.plugWall().isEmpty)
      assert(naturalDefense.plugWall().isEmpty)
      With.placement.restoreCachedWall(None)
      buildings(gateway, forge)
    }
    // Exercise launch, recruitment, gathering and the actual squad batch swap together.
    With.self = player(Race.Protoss)
    set(With.self, "bwapiPlayer", allocate(classOf[bwapi.WorkerRecruitmentPlayer]))
    With.performance = new Performance.PerformanceMonitor
    With.fingerprints = new Information.Fingerprinting.Fingerprints
    With.battles = allocate(classOf[Information.Battles.Battles])
    With.battles.byUnit = Map.empty
    With.matchups = new Micro.Matchups.MatchupGraph
    With.scouting = allocate(classOf[Information.Scouting.Scouting])
    set(With.scouting, "_enemyMuscleOrigin", new Cache(() => base.townHallTile))
    set(With.game, "startLocations", new java.util.ArrayList[bwapi.TilePosition]())
    set(With.latency, "_remainingFrames", 2)
    val recruitmentBase = new Base("Recruitment", base.townHallTile, Set.empty)
    recruitmentBase.naturalOf = Some(main)
    recruitmentBase.owner = With.self
    val recruitmentMetro = new Information.Geography.Types.Metro(Vector(recruitmentBase)) {
      override lazy val bases = Vector(recruitmentBase)
      override lazy val zones = Vector(zone)
    }
    set(With.geography, "_metros", Vector(recruitmentMetro))
    set(With.geography, "_bases", Vector(recruitmentBase))
    set(With.geography, "_edges", Vector.empty)
    set(With.geography, "_baseByTile", Array.fill[Option[Base]](With.mapTileArea)(Some(recruitmentBase)))
    set(With.geography, "ourMainCache", new Cache(() => recruitmentBase))
    set(With.geography, "ourFoyerCache", new Cache(() => recruitmentBase))
    set(zone, "zones", Vector(zone))
    set(zone, "_exitCache", new Cache(() => None))
    def recruit(id: Int, kind: UnitClass = Protoss.Probe): FriendlyUnitInfo = {
      val u = unit(id, kind, Tile(14, 18))
      set(u, "_player", With.self)
      set(u, "_powered", true)
      set(u, "_squad", None)
      set(u, "_intent", new Intention)
      set(u, "canAttackCache", new Cache(() => kind.rawCanAttack))
      set(u, "attacksAgainstGroundCache", new Cache(() => if (kind.attacksGround) 1 else 0))
      set(u, "_confidence", new Cache(() => -0.5))
      u
    }
    val miners = (20 until 30).map(recruit(_)).toVector
    val fighter = recruit(30, Protoss.Zealot)
    val marine = recruit(31, ProxyBwapi.Races.Terran.Marine)
    val enemyZealot = recruit(32, Protoss.Zealot)
    val firebat = recruit(33, ProxyBwapi.Races.Terran.Firebat)
    class RecruitmentSquad extends SquadDefendBase(recruitmentBase) {
      override def run(): Unit = units.foreach(_.intend(this).setTerminus(recruitmentBase.townHallTile.center))
    }
    val squadsRun = classOf[Tactic.Squads.Squads].getDeclaredMethods.find(_.getName == "onRun").get
    squadsRun.setAccessible(true)
    def resetRecruitment(wall: Boolean = false): (RecruitmentSquad, Planning.ResourceLocks.LockUnits) = {
      With.frame = 5000
      With.priorities = new Macro.Allocation.Priorities
      With.recruiter = new Macro.Allocation.Recruiter
      With.squads = new Tactic.Squads.Squads
      placement()
      buildings((miners ++ Vector(fighter) ++ (if (wall) Vector(gateway, forge) else Vector.empty)): _*)
      With.recruiter.update()
      With.priorities.update()
      val gather = new Tactic.Tactics.TacticGather
      (new RecruitmentSquad, gather.workerLock)
    }
    def nextCycle(): Unit = {
      With.frame += 1
      With.recruiter.update()
      With.priorities.update()
    }
    check("recruitment after gathering cannot take miners, but launch before gathering can") {
      val (squad, gather) = resetRecruitment()
      squad.setEnemies(Vector(marine))
      gather.acquire()
      squad.launch()
      assert(squad.workerLock.units.isEmpty)
      nextCycle()
      squad.launch()
      assert(squad.workerLock.units.size == 2)
      val pulled = squad.workerLock.units.toSet
      gather.acquire()
      assert(pulled.intersect(gather.units.toSet).isEmpty)
      squadsRun.invoke(With.squads, Long.box(1000))
      assert(squad.units.toSet == pulled)
      assert(squad.enemies == Vector(marine))
      assert(pulled.forall(u => u.squad.contains(squad) && u.client == squad && u.intent.frameCreated == With.frame))
    }
    check("defense recruitment respects workers already claimed by priority tactics") {
      val (squad, _) = resetRecruitment()
      val priority = new Tactic.Tactics.TacticGather
      priority.workerLock.setCounter(Utilities.UnitCounters.CountUpTo(1)).acquire()
      val reserved = priority.workerLock.units.toSet
      squad.setEnemies(Vector(marine))
      squad.launch()
      assert(squad.workerLock.units.size == 2)
      assert(squad.workerLock.units.toSet.intersect(reserved).isEmpty)
      assert(priority.workerLock.units.toSet == reserved)
    }
    check("invasion recruitment reads pending defenders instead of the previous roster") {
      val (squad, _) = resetRecruitment()
      squad.setEnemies(Vector(marine))
      set(fighter, "_confidence", new Cache(() => 0.5))
      squad.addUnit(fighter)
      squad.launch()
      assert(squad.workerLock.units.isEmpty)
      set(fighter, "_confidence", new Cache(() => -0.5))
      squad.launch()
      assert(squad.workerLock.units.size == 2)
    }
    check("pure Zealot invasions and Firebats retain the existing no-pull policy") {
      Vector(Vector(enemyZealot), Vector(marine, firebat)).foreach(invaders => {
        val (squad, _) = resetRecruitment()
        squad.setEnemies(invaders)
        squad.launch()
        assert(squad.workerLock.units.isEmpty)
      })
    }
    check("an FFE wall can recruit its first Probes without a standing army") {
      val (squad, gather) = resetRecruitment(wall = true)
      val minimum = new WallGapPositions(recruitmentBase).minimumDefenders.get
      assert(minimum > 0)
      squad.launch()
      assert(squad.workerLock.units.size == minimum)
      val pulled = squad.workerLock.units.toSet
      gather.acquire()
      squadsRun.invoke(With.squads, Long.box(1000))
      nextCycle()
      squad.launch()
      assert(squad.workerLock.units.toSet == pulled)
      gather.acquire()
      squadsRun.invoke(With.squads, Long.box(1000))
      assert(squad.units.toSet == pulled)
    }
    check("reinforcing Zealots release unneeded Probes for gathering in the same cycle") {
      val (squad, gather) = resetRecruitment(wall = true)
      squad.launch()
      assert(squad.workerLock.units.nonEmpty)
      squadsRun.invoke(With.squads, Long.box(1000))
      nextCycle()
      val minimum = new WallGapPositions(recruitmentBase).minimumDefenders.get
      Vector.fill(minimum)(()).zipWithIndex.foreach { case (_, i) => squad.addUnit(recruit(40 + i, Protoss.Zealot)) }
      squad.launch()
      assert(squad.workerLock.units.isEmpty)
      gather.acquire()
      assert(miners.forall(gather.units.contains))
    }
    check("workers return to mining when an invasion ends or the time limit expires") {
      Vector(false, true).foreach(expire => {
        val (squad, gather) = resetRecruitment()
        squad.setEnemies(Vector(marine))
        squad.launch()
        assert(squad.workerLock.units.nonEmpty)
        squadsRun.invoke(With.squads, Long.box(1000))
        nextCycle()
        if (expire) {
          With.frame = Utilities.Time.Minutes(8)()
          squad.setEnemies(Vector(marine))
        }
        squad.launch()
        assert(squad.workerLock.units.isEmpty)
        gather.acquire()
        assert(miners.forall(gather.units.contains))
      })
    }
    println(s"$passed wall defense integration tests passed")
  }
}
