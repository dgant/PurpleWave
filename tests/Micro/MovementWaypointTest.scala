package bwta {
  // Allocated without running the native map constructor.
  class MovementTestChoke extends Chokepoint(null) {
    var regions: bwapi.Pair[Region, Region] = _
    override def getRegions: bwapi.Pair[Region, Region] = regions
  }
}

package Micro {
  import Information.Geography.Geography
  import Information.Geography.Pathfinding.Paths
  import Information.Geography.Types.{Base, Edge, Zone}
  import Information.Grids.Grids
  import Information.Grids.Movement.{GridUnwalkableUnits, GridWalkable, GridWalkableTerrain}
  import Lifecycle.With
  import Lifecycle.Configure.Configuration
  import Macro.Allocation.Prioritized
  import Mathematics.Points.{Pixel, Tile}
  import Micro.Actions.Combat.Maneuvering.DownhillPathfinder
  import Micro.Agency.{Commander, Intention}
  import Performance.{Cache, ReactionTimes}
  import Planning.ResourceLocks.LockUnits
  import ProxyBwapi.ProxyBWAPI
  import ProxyBwapi.Races.Protoss
  import ProxyBwapi.UnitClasses.{UnitClass, UnitTypes}
  import ProxyBwapi.UnitInfo.FriendlyUnitInfo
  import ProxyBwapi.UnitTracking.UnitTracker
  import Utilities.UnitCounters.CountOne
  import Utilities.UnitPreferences.PreferClose

  object MovementWaypointTest extends App {
    val unsafeField = classOf[sun.misc.Unsafe].getDeclaredField("theUnsafe")
    unsafeField.setAccessible(true)
    val unsafe = unsafeField.get(null).asInstanceOf[sun.misc.Unsafe]
    def allocate[T](clazz: Class[T]): T = unsafe.allocateInstance(clazz).asInstanceOf[T]
    def set(obj: AnyRef, name: String, value: Any): Unit = {
      val fields = Iterator.iterate[Class[_]](obj.getClass)(_.getSuperclass).takeWhile(_ != null).flatMap(_.getDeclaredFields)
      val field = fields.find(f => f.getName == name || f.getName.endsWith("$$" + name)).get
      field.setAccessible(true)
      field.set(obj, value.asInstanceOf[AnyRef])
    }
    var passed = 0
    def check(name: String)(body: => Unit): Unit = { body; passed += 1; println("PASS " + name) }

    With.frame = 96 // Together with unit id zero, disables the existing +/- 1 destination noise.
    With.configuration = new Configuration
    With.performance = new Performance.PerformanceMonitor
    With.mapTileWidth = 128; With.mapTileHeight = 128; With.mapTileArea = 128 * 128
    With.mapPixelWidth = 4096; With.mapPixelHeight = 4096
    With.proxy = allocate(classOf[ProxyBWAPI])
    set(With.proxy, "unitClasses", UnitTypes.all.map(UnitClass))
    With.units = new UnitTracker
    With.reaction = new ReactionTimes { override def sluggishness: Int = 0 }
    With.geography = allocate(classOf[Geography])
    With.grids = allocate(classOf[Grids])
    val terrain = new GridWalkableTerrain
    terrain.indices.foreach(terrain.set(_, true))
    set(With.grids, "walkableTerrain", terrain)
    set(With.grids, "walkable", new GridWalkable)
    set(With.grids, "unwalkableUnits", new GridUnwalkableUnits)
    With.paths = new Paths

    // A static distance field points north through the future building row.
    val main = allocate(classOf[Zone])
    val outside = allocate(classOf[Zone])
    Seq(main, outside).foreach(z => set(z, "zones", Vector(z)))
    set(With.geography, "_zoneByTile", Array.tabulate(With.mapTileArea)(i => if (new Tile(i).y >= 96) main else outside))
    set(With.geography, "_bases", Vector.empty[Base])
    val mainRegion = allocate(classOf[bwta.Region])
    val outsideRegion = allocate(classOf[bwta.Region])
    set(mainRegion, "center", Pixel(239, 3503).bwapi)
    set(outsideRegion, "center", Pixel(341, 2458).bwapi)
    val choke = allocate(classOf[bwta.MovementTestChoke])
    choke.regions = new bwapi.Pair(mainRegion, outsideRegion)
    set(choke, "width", 1.0)
    set(choke, "center", Pixel(239, 3119).bwapi)
    set(choke, "sides", new bwapi.Pair(Pixel(239, 3119).bwapi, Pixel(240, 3119).bwapi))
    val edge = new Edge(choke)
    set(With.geography, "_edges", Vector(edge))
    edge.distanceGrid.initialize()

    def unit(kind: UnitClass, at: Pixel): FriendlyUnitInfo = {
      val u = allocate(classOf[FriendlyUnitInfo])
      set(u, "id", 0)
      set(u, "_unitClass", kind)
      set(u, "_pixel", at); set(u, "_tile", at.tile)
      set(u, "_flying", kind.isFlyer)
      set(u, "_transport", None); set(u, "friendly", Some(u))
      set(u, "_loadedUnitsCache", new Cache(() => Vector.empty[FriendlyUnitInfo]))
      set(u, "_intent", new Intention)
      u
    }
    def building(x: Int, y: Int, width: Int, height: Int): Unit = {
      for (dx <- 0 until width; dy <- 0 until height) With.grids.unwalkableUnits.stamp(x + dx, y + dy)
    }
    val goal = Pixel(341, 2458)
    // Pylon and Gateway footprints from GAME_PB_F203670D99_0115 on Gladiator.
    building(5, 116, 2, 2)
    building(7, 114, 4, 3)

    check("a greedy route can succeed before a tile boundary and fail immediately after it") {
      assert(DownhillPathfinder.decend(Tile(5, 118), goal.tile).nonEmpty)
      assert(DownhillPathfinder.decend(Tile(6, 118), goal.tile).isEmpty)
    }
    check("successful waypoints beside buildings keep the fast path") {
      val u = unit(Protoss.Dragoon, Pixel(170, 3780))
      assert(Commander.adjustDestination(u, goal, remember = true) == Pixel(240, 3760))
      assert(u.agent.waypointFallback.isEmpty)
    }
    check("a failed waypoint retains the detour across the reversal boundary") {
      val u = unit(Protoss.Dragoon, Pixel(210, 3780))
      assert(Commander.adjustDestination(u, goal, remember = true) == goal)
      assert(u.agent.waypointFallback.isDefined)
      for (x <- Seq(200, 192, 191, 185, 170, 155)) {
        set(u, "_pixel", Pixel(x, 3780)); set(u, "_tile", u.pixel.tile)
        assert(Commander.adjustDestination(u, goal, remember = true) == goal, s"Reversing the engine's detour at ${u.pixel}")
      }
      set(u, "_pixel", Pixel(128, 3500)); set(u, "_tile", u.pixel.tile)
      assert(Commander.adjustDestination(u, goal, remember = true) != goal)
      assert(u.agent.waypointFallback.isEmpty)
    }
    check("planning queries do not start or overwrite a movement detour") {
      val u = unit(Protoss.Dragoon, Pixel(210, 3780))
      assert(Commander.adjustDestination(u, goal) == goal)
      assert(u.agent.waypointFallback.isEmpty)
      Commander.adjustDestination(u, goal, remember = true)
      val fallback = u.agent.waypointFallback
      Commander.adjustDestination(u, Pixel(800, 3800))
      assert(u.agent.waypointFallback == fallback)
    }
    check("a changed goal replaces a detour immediately") {
      val u = unit(Protoss.Dragoon, Pixel(210, 3780))
      Commander.adjustDestination(u, goal, remember = true)
      val newGoal = Pixel(128, 2458)
      assert(Commander.adjustDestination(u, newGoal, remember = true) != goal)
      assert(!u.agent.waypointFallback.exists(_._1 == goal))
    }
    check("short tactical commands cancel a detour immediately") {
      val u = unit(Protoss.Dragoon, Pixel(210, 3780))
      Commander.adjustDestination(u, goal, remember = true)
      val near = Pixel(239, 3759)
      assert(Commander.adjustDestination(u, near, remember = true) == near)
      assert(u.agent.waypointFallback.isEmpty)
    }
    check("nearby short tactical moves retain their exact destination") {
      val near = Pixel(239, 3759)
      assert(Commander.adjustDestination(unit(Protoss.Dragoon, Pixel(170, 3780)), near) == near)
    }
    check("same-zone travel beside buildings retains its destination") {
      val sameZone = Pixel(800, 3800)
      assert(Commander.adjustDestination(unit(Protoss.Dragoon, Pixel(170, 3780)), sameZone) == sameZone)
    }
    check("air movement keeps its destination and acceleration behavior") {
      val u = unit(Protoss.Shuttle, Pixel(170, 3780))
      assert(Commander.adjustDestination(u, goal) == goal)
      val short = Pixel(202, 3780)
      assert(Commander.adjustDestination(u, short) == Pixel(458, 3780))
    }
    check("travel away from buildings still receives a terrain waypoint") {
      val u = unit(Protoss.Dragoon, Pixel(500, 3800))
      assert(Commander.adjustDestination(u, goal) != goal)
    }
    check("removing buildings restores the usual waypoint selection") {
      With.grids.unwalkableUnits.reset()
      val u = unit(Protoss.Dragoon, Pixel(170, 3780))
      val next = Commander.adjustDestination(u, goal)
      assert(next != goal && u.pixel.pixelDistance(next) <= 128)
    }
    check("terrain obstacles continue to use the existing terrain route") {
      for (x <- 5 to 6; y <- 116 to 117) terrain.set(Tile(x, y), false)
      val u = unit(Protoss.Dragoon, Pixel(170, 3780))
      assert(Commander.adjustDestination(u, goal) == Pixel(239, 3759))
    }
    terrain.indices.foreach(terrain.set(_, true))
    With.grids.unwalkableUnits.reset()
    // Recorded Destination minerals: bounds (832,288)-(895,319) and (832,352)-(895,383).
    building(26, 9, 2, 1)
    building(26, 11, 2, 1)
    check("a physically fitting Dragoon waypoint must not become an engine mineral target") {
      val u = unit(Protoss.Dragoon, Pixel(783, 341))
      val waypoint = Pixel(879, 335)
      assert(u.unitClass.cornersInclusive.forall(c => waypoint.add(c).walkable))
      assert(Commander.findGroundWaypointWithClearance(u, waypoint).isEmpty)
    }
    check("smaller units retain a waypoint through the same mineral gap") {
      val u = unit(Protoss.Zealot, Pixel(783, 341))
      val waypoint = Pixel(879, 335)
      assert(Commander.findGroundWaypointWithClearance(u, waypoint).contains(waypoint))
    }
    check("a waypoint just beyond the minerals retains its exact destination") {
      val u = unit(Protoss.Dragoon, Pixel(815, 335))
      val waypoint = Pixel(912, 335)
      assert(Commander.findGroundWaypointWithClearance(u, waypoint).contains(waypoint))
    }
    With.grids.unwalkableUnits.reset()
    building(4, 115, 2, 3)
    building(7, 115, 2, 3)
    check("a successful route into a tight gap falls back immediately while the unit is moving") {
      for (x <- Seq(206, 207, 208, 207, 206)) {
        val u = unit(Protoss.Dragoon, Pixel(x, 3855))
        assert(DownhillPathfinder.decend(u.tile, goal.tile).nonEmpty)
        assert(Commander.adjustDestination(u, goal) == goal)
        assert(u.agent.waypointFallback.isEmpty)
        assert(Commander.adjustDestination(u, goal, remember = true) == goal)
        assert(u.agent.waypointFallback.isDefined)
      }
    }
    check("recruitment keeps ground-path proximity when the performance alarm is set") {
      val destination = Tile(100, 90).center
      val acrossBoundary = unit(Protoss.Dragoon, Tile(100, 100).center)
      val approaching = unit(Protoss.Dragoon, Tile(80, 90).center)
      val lock = new LockUnits(new Prioritized {}).setCounter(CountOne).setPreference(PreferClose(destination))
      assert(acrossBoundary.pixelDistanceCenter(destination) < approaching.pixelDistanceCenter(destination))
      assert(acrossBoundary.pixelDistanceTravelling(destination) > approaching.pixelDistanceTravelling(destination))
      for (slowFrames <- Seq(0, 160)) {
        With.performance.framesOverShort = slowFrames
        assert(lock.offerUnits(Vector(approaching, acrossBoundary), dryRun = true).get.head eq approaching)
      }
    }
    check("walkable pixels stay inside the selected tile on every side of a blocker") {
      With.grids.unwalkableUnits.reset()
      val blocked = Tile(120, 3)
      for (open <- blocked.adjacent4) {
        blocked.adjacent9.foreach(terrain.set(_, false))
        terrain.set(open, true)
        for (x <- 0 until 32; y <- 0 until 32) {
          val source = blocked.topLeftPixel.add(x, y)
          val projected = source.walkablePixel
          assert(projected.walkable, s"$source projected outside $open to $projected")
          assert(projected.tile == open && projected.walkablePixel == projected)
        }
      }
      blocked.adjacent9.foreach(terrain.set(_, true))
    }
    println(s"$passed movement waypoint checks passed")
  }
}
