package Placement.Generation

import Information.Geography.Types.Zone
import Lifecycle.With
import Mathematics.Points.{Direction, SpecificPoints}

trait Generator extends Fitter {

  private var _initialized: Boolean = false

  protected def initialize(): Unit = {
    if (_initialized) return
    _initialized = true
    With.units.neutral
      .filter(_.unitClass.isBuilding)
      .filterNot(u => u.base.exists(_.townHallArea.intersects(u.tileArea)))
      .foreach(_.tileArea.tiles.map(Fit(_, Templates.walkway)).foreach(index))
    With.geography.bases.foreach(b => index(Fit(b.townHallTile, Templates.townhall)))
    With.geography.bases.foreach(_.resourcePathTiles.foreach(t => index(Fit(t, Templates.walkway))))
    With.geography.zones.foreach(preplaceWalls)
    With.geography.zones.foreach(preplaceZone)
    sort()
  }

  private def preplaceZone(zone: Zone): Unit = {
    val bounds = zone.boundary
    if (bounds.cornerTilesInclusive.distinct.length < 4) return // Alchemist has a degenerate 1xN zone
    val exitDirection     = zone.exitOriginal.map(_.direction).getOrElse(zone.centroid.subtract(SpecificPoints.tileMiddle).direction)
    val exitTile          = zone.exitOriginal.map(_.pixelCenter.tile).getOrElse(zone.centroid)
    val tilesFront        = bounds.cornerTilesInclusive.sortBy(t => Math.min(Math.abs(t.x - exitTile.x), Math.abs(t.y - exitTile.y))).take(2)
    val tilesBack         = bounds.cornerTilesInclusive.filterNot(tilesFront.contains)
    val cornerFront       = tilesFront.maxBy(_.tileDistanceSquared(SpecificPoints.tileMiddle))
    val cornerBack        = tilesBack.maxBy(_.tileDistanceSquared(cornerFront))
    val directionToBack   = new Direction(cornerFront, cornerBack)
    val directionToFront  = new Direction(cornerBack, cornerFront)
    fitAndIndex     (1, exitTile,    bounds, directionToBack,  Templates.batterycannon)
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
