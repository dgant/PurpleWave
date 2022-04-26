package Placement

import Information.Geography.Types.Zone
import Lifecycle.With
import Mathematics.Points.{Direction, Directions, SpecificPoints, TileRectangle}
import Placement.Generation.{Fit, Fitter, Templates, TerranWall}

class Placement extends Fitter {

  private var _initialized: Boolean = false

  def initialize(): Unit = {
    if (_initialized) return
    _initialized = true
    With.units.neutral
      .filter(_.unitClass.isBuilding)
      .filterNot(u => u.base.exists(_.townHallArea.intersects(u.tileArea)))
      .foreach(_.tileArea.tiles.map(Fit(_, Templates.walkway)).foreach(index))
    // Fit closer zones first, so in case of tiebreaks we prefer closer zones
    val basesSorted = With.geography.bases.sortBy(_.heart.groundTiles(With.geography.home))
    val zonesSorted = With.geography.zones.sortBy(_.heart.groundTiles(With.geography.home))
    basesSorted.foreach(b => index(Fit(b.townHallTile, Templates.townhall)))
    zonesSorted.sortBy(_.heart.groundTiles(With.geography.home)).foreach(preplaceWalls)
    basesSorted.foreach(base => fitAndIndexAll(5, base.tiles.minBy(_.i), new TileRectangle(base.tiles), Directions.Down, if (base.isStartLocation) Templates.mainBases else Templates.bases))
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
    val cornerFront       = tilesFront.maxBy(_.tileDistanceSquared(SpecificPoints.tileMiddle))
    val cornerBack        = tilesBack.maxBy(_.tileDistanceSquared(cornerFront))
    val directionToBack   = new Direction(cornerFront, cornerBack)
    val directionToFront  = new Direction(cornerBack, cornerFront)
    fitAndIndexAll  (1, exitTile,    bounds, directionToBack,  Templates.batterycannon)
    fitAndIndexAll  (0, cornerFront, bounds, directionToBack,  Templates.initialLayouts)
    fitAndIndexAll  (3, cornerFront, bounds, directionToBack,  Templates.gateways, 2)
    fitAndIndexAll  (2, cornerBack,  bounds, directionToFront, Templates.tech)
    fitAndIndexAll  (4, cornerBack,  bounds, directionToFront, Templates.tech)
    fitAndIndexAll  (4, cornerFront, bounds, directionToBack,  Templates.gateways, 5)
  }

  private def preplaceWalls(zone: Zone): Unit = {
    // DISABLED. Terran walls are too slow to use as-is
    if (With.self.isTerran && false) {
      TerranWall(zone).foreach(index)
    }
  }
}
