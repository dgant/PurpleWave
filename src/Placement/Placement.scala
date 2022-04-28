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
    basesSorted.foreach(base => fitAndIndexConstrained(5, 1, if (base.isStartLocation) Templates.mainBases else Templates.bases, base))
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
    fitAndIndexRectangle(1, 1, Templates.batterycannon,  exitTile,    bounds, directionToBack)
    fitAndIndexRectangle(0, 1, Templates.initialLayouts, cornerFront, bounds, directionToBack)
    fitAndIndexRectangle(2, 1, Templates.tech,           cornerBack,  bounds, directionToFront)
    fitAndIndexRectangle(3, 1, Templates.gateways,       cornerFront, bounds, directionToBack)
    fitAndIndexRectangle(4, 1, Templates.tech,           cornerBack,  bounds, directionToFront)
    fitAndIndexRectangle(4, 5, Templates.gateways,       cornerFront, bounds, directionToBack)
  }

  private def preplaceWalls(zone: Zone): Unit = {
    if (With.self.isTerran && zone.metro.contains(With.geography.ourMain.metro)) {
      TerranWall(zone).foreach(index)
    }
  }
}
