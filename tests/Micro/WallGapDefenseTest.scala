package bwapi {
  class WallGapTestUnit extends Unit(null, 0, null) {
    var movedTo: Position = _
    var holds: Int = 0
    var stops: Int = 0
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
    val positions = new WallGaps(base)
    check("minimum defenders is available before assignment and refreshes when the wall changes") {
      val wallGaps = new WallGaps(base)
      val minimum = wallGaps.minimumDefenders
      assert(minimum.exists(_ > 0), minimum)
      assert(new WallGaps(main).minimumDefenders.isEmpty)
      buildings(gateway)
      assert(wallGaps.minimumDefenders.isEmpty)
      buildings(gateway, forge)
      assert(wallGaps.minimumDefenders == minimum)
      With.enemies = Vector(terran)
      assert(wallGaps.minimumDefenders.isEmpty)
      With.enemies = Vector(zerg)
      assert(wallGaps.minimumDefenders == minimum)
    }
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
      assert(new WallGaps(main)(defenders).isEmpty)
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
      val obstacle = WallGaps.Box(blocker.left, blocker.top, blocker.rightExclusive, blocker.bottomExclusive)
      after.foreach { case (u, p) =>
        val kind = u.unitClass
        assert(! WallGaps.Body(kind.dimensionLeft, kind.dimensionUp, kind.dimensionRightExclusive, kind.dimensionDownExclusive).at(p).intersects(obstacle))
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
          squad.intendHoldWallGap(defender, target)
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
      assert(mainDefense.wallToPlug().contains(wall.defensePoint))
      assert(naturalDefense.wallToPlug().contains(wall.defensePoint))
      assert(expansionDefense.wallToPlug().isEmpty)
      expansionDefense.run()
      assert(expansionDefense.vicinity == remote.heart.center)
      buildings()
      With.frame += 1
      assert(mainDefense.wallToPlug().isEmpty)
      assert(naturalDefense.wallToPlug().isEmpty)
      With.placement.restoreCachedWall(None)
      buildings(gateway, forge)
    }
    println(s"$passed wall defense integration tests passed")
  }
}
