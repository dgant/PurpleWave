package Placement

import Information.Geography.Types.Zone
import Lifecycle.With
import Mathematics.Points.{Direction, SpecificPoints}
import Performance.Tasks.TimedTask
import Placement.Generation.{TerranWall, Templates}
import Placement.Templating.{Fit, Fitter}

class Preplacement extends TimedTask with Fitter {

  private var initialized: Boolean = false

  override protected def onRun(budgetMs: Long): Unit = {
    if (initialized) return
    initialized = true
    With.units.neutral
      .filter(_.unitClass.isBuilding)
      .filterNot(u => u.base.exists(_.townHallArea.intersects(u.tileArea)))
      .foreach(_.tileArea.tiles.map(Fit(_, Templates.walkway)).foreach(place))
    With.geography.bases.foreach(b => place(Fit(b.townHallTile, Templates.townhall)))
    With.geography.bases.foreach(_.resourcePathTiles.foreach(t => place(Fit(t, Templates.walkway))))
    With.geography.zones.foreach(preplaceWalls)
    With.geography.zones.foreach(preplaceZone)
  }

  private def preplaceZone(zone: Zone): Unit = {
    val bounds = zone.boundary
    if (bounds.cornerTilesInclusive.distinct.length < 4) return // Alchemist has a degenerate 1xN zone
    val exitDirection     = zone.exit.map(_.direction).getOrElse(zone.centroid.subtract(SpecificPoints.tileMiddle).direction)
    val exitTile          = zone.exit.map(_.pixelCenter.tile).getOrElse(zone.centroid)
    val tilesFront        = bounds.cornerTilesInclusive.sortBy(t => Math.min(Math.abs(t.x - exitTile.x), Math.abs(t.y - exitTile.y))).take(2)
    val tilesBack         = bounds.cornerTilesInclusive.filterNot(tilesFront.contains)
    val cornerFront       = tilesFront.maxBy(_.tileDistanceSquared(SpecificPoints.tileMiddle))
    val cornerBack        = tilesBack.maxBy(_.tileDistanceSquared(cornerFront))
    val directionToBack   = new Direction(cornerFront, cornerBack)
    val directionToFront  = new Direction(cornerBack, cornerFront)
    addFits(fitAndPlace     (exitTile,    bounds, directionToBack,  Templates.batterycannon))
    addFits(fitAndPlaceAll  (cornerFront, bounds, directionToBack,  Templates.initialLayouts))
    addFits(fitAndPlaceAll  (cornerFront, bounds, directionToBack,  Templates.gateways))
    addFits(fitAndPlaceAll  (cornerBack,  bounds, directionToFront, Templates.tech))
    addFits(fitAndPlaceAll  (cornerFront, bounds, directionToBack,  Templates.gateways, 2))
    addFits(fitAndPlaceAll  (cornerBack,  bounds, directionToFront, Templates.tech))
    addFits(fitAndPlaceAll  (cornerFront, bounds, directionToBack,  Templates.gateways, 5))
  }

  private def preplaceWalls(zone: Zone): Unit = {
    // DISABLED. Terran walls are too slow to use as-is
    if (With.self.isTerran && false) {
      val terranWall = TerranWall(zone)
      terranWall.foreach(addFit)
      terranWall.foreach(place)
    }
  }
}
