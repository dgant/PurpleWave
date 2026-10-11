package Information.Geography

import Information.Geography.Calculations.UpdateZones
import Information.Geography.Pathfinding.Paths
import Information.Geography.Pathfinding.Types.{ZonePath, ZonePathNode}
import Information.Geography.Types.{Base, ChokeGeometry, Edge, Zone}
import Information.Grids.Grids
import Information.Grids.Movement.{GridGroundDistance, GridUnwalkableUnits, GridWalkable, GridWalkableTerrain}
import Information.Grids.Vision.{GridScoutingPathsBases, GridScoutingPathsStartLocations}
import Lifecycle.With
import Mathematics.Points.{Pixel, Tile}
import Micro.Formation.FormationFlanking
import ProxyBwapi.ProxyBWAPI
import ProxyBwapi.UnitClasses.{UnitClass, UnitTypes}
import ProxyBwapi.UnitInfo.ForeignUnitInfo
import ProxyBwapi.UnitTracking.{UnitTracker, UnorderedBuffer}
import bwapi.GameBuilder
import bwta.BWTA

import scala.collection.JavaConverters._
import scala.collection.mutable

object FormationPathTest extends App {
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
  def check(name: String)(test: => Unit): Unit = {
    test
    passed += 1
    println("PASS " + name)
  }

  val bridge = allocate(classOf[Edge])
  val third = allocate(classOf[Edge])
  def route(length: Double, edges: Edge*): ZonePath = {
    val distance = length
    new ZonePath(null, null, edges.map(ZonePathNode(null, _)).toVector) {
      override lazy val length: Double = distance
    }
  }
  val direct = route(100, bridge)
  val detour = route(250, third)
  def exposedCost(step: ZonePathNode): Double = if (step.edge == bridge) 9 else 1

  check("a longer safe final approach beats crossing an exposed narrow bridge") {
    val selected = FormationFlanking.choose(Some(direct), Seq(detour), exposedCost)
    assert(selected.path.contains(detour) && selected.flanking)
  }
  check("an unthreatened narrow crossing does not even search for flanks") {
    val selected = FormationFlanking.choose(Some(direct), throw new AssertionError("unnecessary route search"), _ => 1)
    assert(selected.path.contains(direct) && ! selected.flanking)
  }
  check("narrow intermediate crossings do not penalize a safe final approach") {
    val selected = FormationFlanking.choose(Some(direct), Seq(route(250, bridge, third)), exposedCost)
    assert(selected.flanking && selected.path.get.steps.last.edge == third)
  }
  check("missing or worse alternate routes retain the direct route") {
    assert(FormationFlanking.choose(Some(direct), Seq.empty, exposedCost).path.contains(direct))
    assert(! FormationFlanking.choose(Some(direct), Seq(route(1000, third)), exposedCost).flanking)
  }
  check("an alternate approach remains a flank even when it is shorter") {
    assert(FormationFlanking.choose(Some(direct), Seq(route(80, third)), exposedCost).flanking)
  }
  check("an absent route and a same-zone route are safe") {
    assert(FormationFlanking.choose(None, Seq.empty, exposedCost).path.isEmpty)
    assert(! FormationFlanking.choose(Some(route(0)), Seq.empty, exposedCost).flanking)
  }
  check("a point choke gets a terrain cross-section, not a one-pixel width") {
    val sides = ChokeGeometry.sides(Pixel(512, 512), p => p.y >= 480 && p.y < 544)
    val width = sides.head.pixelDistance(sides.last)
    assert(width >= 64 && width <= 80, width)
  }

  def loadMap(name: String): UnorderedBuffer[ForeignUnitInfo] = {
    With.game = GameBuilder.createGame(name)
    With.frame = 100
    With.mapCleanName = name.toLowerCase
    With.mapTileWidth = With.game.mapWidth()
    With.mapTileHeight = With.game.mapHeight()
    With.mapTileArea = With.mapTileWidth * With.mapTileHeight
    With.mapPixelWidth = With.mapTileWidth * 32
    With.mapPixelHeight = With.mapTileHeight * 32
    With.units = new UnitTracker
    With.proxy = allocate(classOf[ProxyBWAPI])
    set(With.proxy, "unitClasses", UnitTypes.all.map(UnitClass))
    val neutrals = new UnorderedBuffer[ForeignUnitInfo]
    With.game.getNeutralUnits.asScala.filter(_.getType.isBuilding).foreach(u => {
      val unit = allocate(classOf[ForeignUnitInfo])
      set(unit, "_unitClass", UnitClass(u.getType))
      set(unit, "_tileTopLeft", new Tile(u.getTilePosition))
      set(unit, "_flying", false)
      neutrals.add(unit)
    })
    set(With.units, "bufferNeutral", neutrals)
    With.grids = allocate(classOf[Grids])
    set(With.grids, "walkable", new GridWalkable)
    set(With.grids, "walkableTerrain", new GridWalkableTerrain)
    set(With.grids, "unwalkableUnits", new GridUnwalkableUnits)
    set(With.grids, "scoutingPathsBases", new GridScoutingPathsBases)
    set(With.grids, "scoutingPathsStartLocations", new GridScoutingPathsStartLocations)
    With.grids.walkableTerrain.initialize()
    With.grids.unwalkableUnits.update()
    With.paths = new Paths
    BWTA.readMap(With.game)
    BWTA.analyze()
    With.geography = allocate(classOf[Geography])
    val tiles = (0 until With.mapTileArea).map(new Tile(_)).toVector
    val tilesByRegion = tiles.groupBy(t => BWTA.getRegion(t.bwapi))
    val zones = BWTA.getRegions.asScala.map(r => new Zone(r.getCenter.toString, r, tilesByRegion.getOrElse(r, Vector.empty).toSet)).toVector
    set(With.geography, "allTiles", tiles)
    set(With.geography, "_zones", zones)
    set(With.geography, "_bases", Vector.empty[Base])
    set(With.geography, "_baseByTile", Array.fill[Option[Base]](With.mapTileArea)(None))
    set(With.geography, "_zoneByTile", tiles.map(t => zones.find(_.bwemRegion == BWTA.getRegion(t.bwapi)).get).toArray)
    set(With.geography, "_edges", BWTA.getChokepoints.asScala.map(new Edge(_)).toVector)
    With.geography.edges.foreach(_.distanceGrid.initialize())
    With.geography.zones.foreach(_.distanceGrid.initialize())
    neutrals
  }

  val neutralUnits = loadMap("(2)Destination.scx")
  val blockerTile = Tile(40, 120)
  val backExit = With.geography.edges.find(_.pixelCenter == Pixel(1312, 3856)).get
  val main = Tile(66, 119).zone
  val backyard = Tile(29, 123).zone
  val initial = With.paths.zonePath(main, backyard).get
  val bridgeGrid = With.geography.edges.minBy(_.pixelCenter.pixelDistanceSquared(Pixel(1456, 3172))).distanceGrid
  val blockers = neutralUnits.filter(_.tileTopLeft == blockerTile).toVector
  check("Destination fixture has the exact 20-mineral back-exit stack") {
    assert(With.game.mapHash() == "4e24f217d2fe4dbfa6799bc57f74d8dc939d425b")
    assert(blockers.size == 20)
    assert(! backExit.isOpen && ! initial.steps.exists(_.edge == backExit))
    assert(bridgeGrid.get(Tile(41, 120)) == 65536)
    assert(backExit.diameterPixels >= 32 && backExit.diameterPixels <= 192, backExit.diameterPixels)
    println(s"Destination back-exit width: ${backExit.diameterPixels}")
  }
  check("removing only part of a stack does not invalidate routing") {
    blockers.dropRight(1).foreach(u => {
      neutralUnits.remove(u)
      With.paths.onNeutralRemoved(u.tiles)
    })
    assert(! With.paths.updateGroundTopology())
    assert(! backExit.isOpen)
    assert(With.paths.zonePath(main, backyard).get eq initial)
  }
  check("the last blocker refreshes distances, cached routes, and flow fields") {
    // A new player building must not get baked into refreshed long-range maps.
    val newBuildingTile = Tile(44, 120)
    With.grids.unwalkableUnits.stamp(newBuildingTile)
    assert(With.paths.groundWalkable(newBuildingTile.i))
    val flow = main.flowField
    val flowTile = backyard.tiles.find(_.walkable).get
    val oldForce = flow.get(flowTile)
    assert(flow.get(flowTile) eq oldForce)
    val oldForces = backyard.tiles.filter(_.walkable).map(t => (t, flow.get(t))).toVector
    neutralUnits.remove(blockers.last)
    With.paths.onNeutralRemoved(blockers.last.tiles)
    assert(! Tile(41, 120).walkable)
    val started = System.nanoTime()
    UpdateZones.updatePaths()
    println(f"Destination topology refresh: ${(System.nanoTime() - started) / 1e6}%.2f ms")
    assert(backExit.isOpen)
    assert(Tile(41, 120).walkable)
    assert(bridgeGrid.get(Tile(41, 120)) == 29, bridgeGrid.get(Tile(41, 120)))
    assert(With.paths.groundWalkable(newBuildingTile.i))
    val newForce = flow.get(flowTile)
    assert(!(newForce eq oldForce))
    assert(flow.get(flowTile) eq newForce)
    assert(oldForces.exists { case (tile, before) =>
      val after = flow.get(tile)
      before.x != after.x || before.y != after.y
    })
    val updated = With.paths.zonePath(main, backyard).get
    assert(!(updated eq initial))
    assert(updated.steps.exists(_.edge == backExit))
    assert(updated.length < 5000, updated.length)
    assert(! With.paths.updateGroundTopology())
  }
  check("bounded distance-grid queue agrees with an independent cardinal BFS") {
    val seed = Tile(66, 119)
    val expected = Array.fill(With.mapTileArea)(65536)
    val queue = mutable.Queue(seed)
    expected(seed.i) = 0
    while (queue.nonEmpty) {
      val here = queue.dequeue()
      here.adjacent4.filter(t => t.valid && With.paths.groundWalkable(t.i) && expected(t.i) == 65536).foreach(t => {
        expected(t.i) = expected(here.i) + 1
        queue.enqueue(t)
      })
    }
    val actual = new GridGroundDistance(seed)
    actual.initialize()
    assert(actual.rawValues.sameElements(expected))
  }

  loadMap("(4)Circuit Breaker.scx")
  check("Circuit Breaker offers wider alternate final approaches to double bridges") {
    val group = new Tactic.Squads.GenericUnitGroup(Vector.empty) {
      override def widthPixels: Double = 800
    }
    val candidates = for {
      start <- With.geography.zones
      end <- With.geography.zones if start != end
      direct <- With.paths.zonePath(start, end).toSeq if direct.steps.nonEmpty && direct.steps.last.edge.diameterPixels < 96
      selected = FormationFlanking.choose(Some(direct), end.edges.flatMap(e => With.paths.zonePath(start, end, Seq(e))), s => s.edge.badness2(group, s.from))
      if selected.flanking && selected.path.get.steps.last.edge.diameterPixels > 128
    } yield (direct, selected.path.get)
    assert(candidates.nonEmpty)
    candidates.take(4).foreach { case (a, b) =>
      println(s"Circuit route ${a.from.centroid} -> ${a.to.centroid}: ${a.steps.map(_.edge.pixelCenter)} -> ${b.steps.map(_.edge.pixelCenter)}")
    }
  }
  println(s"$passed formation/pathfinding checks passed")
}
