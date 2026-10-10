package bwapi {
  class WallGapTestUnit extends Unit(null, 0, null) {
    var movedTo: Position = _
    var holds: Int = 0
    override def move(to: Position): Boolean = { movedTo = to; true }
    override def holdPosition(): Boolean = { holds += 1; true }
  }
}

package Micro.Formation {
  import Information.Geography.Geography
  import Information.Geography.Types.{Base, Zone}
  import Lifecycle.With
  import Lifecycle.Configure.Configuration
  import Mathematics.Points.{Pixel, Point, Tile}
  import Micro.Actions.Combat.Tactics.HoldWallGap
  import Performance.{Cache, Latency}
  import Placement.Placement
  import Placement.Access.PlaceLabels
  import Placement.Generation.Fit
  import Placement.Templating.{RequireWalkable, Template, TemplatePointRequirement}
  import ProxyBwapi.{Orders, ProxyBWAPI}
  import ProxyBwapi.Players.PlayerInfo
  import ProxyBwapi.Races.Protoss
  import ProxyBwapi.UnitClasses.{UnitClass, UnitTypes}
  import ProxyBwapi.UnitInfo.FriendlyUnitInfo
  import ProxyBwapi.UnitTracking.{UnitTracker, UnorderedBuffer}
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
    check("stationary units explicitly hold, keep holding, and move to the exact gap pixel") {
      val target = positions(defenders)(zealot)
      val action = HoldWallGap(target)
      val api = zealot.bwapiUnit.asInstanceOf[bwapi.WallGapTestUnit]
      set(zealot, "_pixel", target)
      set(zealot, "_order", Orders.PlayerGuard)
      action.perform(zealot)
      assert(api.holds == 1)
      zealot.sleepUntil = With.frame
      set(zealot, "_order", Orders.HoldPosition)
      action.perform(zealot)
      assert(api.holds == 1)
      zealot.sleepUntil = With.frame
      set(zealot, "_pixel", target.add(1, 0))
      action.perform(zealot)
      assert(api.movedTo == target.bwapi)
      assert(zealot.agent.decision.pixel.contains(target))
    }
    println(s"$passed wall defense integration tests passed")
  }
}
