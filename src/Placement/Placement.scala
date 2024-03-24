package Placement

import Information.Geography.Types.Zone
import Lifecycle.With
import Mathematics.Points.{Direction, Points}
import Placement.Generation.{Fit, Fitter, Templates}
import Placement.Walls.{Wall, FindWall, WallFinder}
import Utilities.?

class Placement extends Fitter {

  private var _initialized: Boolean = false

  var wallFinder: Option[WallFinder] = None
  val wallEverything = false

  def wall: Option[Wall] = wallFinder.flatMap(_.wall)

  def initialize(): Unit = {
    if (_initialized) return
    _initialized = true

    // Place walkways under neutral buildings
    With.units.neutral
      .filter(_.unitClass.isBuilding)
      .filterNot(u => u.base.exists(_.townHallArea.intersects(u.tileArea)))
      .foreach(_.tileArea.tiles.map(Fit(_, Templates.walkway)).foreach(index))

    // Fit closer zones first, so in case of tiebreaks we prefer closer zones
    val basesSorted = With.geography.bases.sortBy(b => b.heart.groundTiles(With.geography.home))
    val zonesSorted = With.geography.zones.sortBy(z => z.heart.groundTiles(With.geography.home))

    basesSorted.foreach(b => index(Fit(b.townHallTile, Templates.townhall)))
    if (wallEverything) {
      zonesSorted.sortBy(_.heart.groundTiles(With.geography.home)).foreach(preplaceWalls)
    } else if (With.self.isProtoss && With.enemies.exists(_.isZerg)) {
      preplaceWalls(With.geography.ourFoyer.zone)
    }
    basesSorted.foreach(base => fitAndIndexConstrained(5, 1, ?(base.isMain, Templates.mainBases, Templates.bases), base))
    basesSorted.foreach(_.resourcePathTiles.foreach(t => if (at(t).requirement.buildableAfter) index(Fit(t, Templates.walkway))))
    zonesSorted.sortBy(_.heart.groundTiles(With.geography.home)).foreach(preplaceZone)
    sort()
  }

  private def preplaceZone(zone: Zone): Unit = {
    val bounds = zone.boundary
    if (bounds.cornerTilesInclusive.distinct.length < 4) return // Alchemist has a degenerate 1xN zone
    val exitTile          = zone.exitOriginal.map(_.pixelCenter.tile).getOrElse(zone.centroid)
    val tilesFront        = bounds.cornerTilesInclusive.sortBy(t => Math.min(Math.abs(t.x - exitTile.x), Math.abs(t.y - exitTile.y))).take(2)
    val tilesBack         = bounds.cornerTilesInclusive.filterNot(tilesFront.contains)
    val cornerFront       = tilesFront.maxBy(_.tileDistanceSquared(Points.tileMiddle))
    val cornerBack        = tilesBack.maxBy(_.tileDistanceSquared(cornerFront))
    val directionToBack   = new Direction(cornerFront, cornerBack)
    val directionToFront  = new Direction(cornerBack, cornerFront)
    if ( ! zone.isBackyard && ! zone.island) {
      fitAndIndexProximity(1, 1, Templates.batterycannon,  exitTile,      zone, 10)
    }
    fitAndIndexProximity(0, 1, Templates.initialLayouts, zone.downtown, zone)
    fitAndIndexRectangle(2, 1, Templates.tech,           cornerBack,    bounds, directionToFront)
    fitAndIndexRectangle(3, 1, Templates.gateways,       cornerFront,   bounds, directionToBack)
    fitAndIndexRectangle(4, 1, Templates.tech,           cornerBack,    bounds, directionToFront)
    fitAndIndexRectangle(4, 7, Templates.gateways,       cornerFront,   bounds, directionToBack)
  }

  def preplaceWalls(zone: Zone): Unit = {
    wallFinder = FindWall(zone)
    wall.map(_.toFit).foreach(index)
  }
}
