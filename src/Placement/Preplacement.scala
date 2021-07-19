package Placement

import Information.Geography.Types.Zone
import Lifecycle.With
import Mathematics.Points.{Direction, SpecificPoints}
import Performance.Tasks.TimedTask

import scala.collection.mutable.ArrayBuffer

class Preplacement extends TimedTask {

  private var initialized: Boolean = false

  val preplacement: Preplacer = new Preplacer
  val fits = new ArrayBuffer[Fit]

  override protected def onRun(budgetMs: Long): Unit = {
    if (initialized) return
    initialized = true

    With.units.neutral
      .filter(u => u.unitClass.isBuilding && ! u.base.exists(_.townHallArea.intersects(u.tileArea)))
      .foreach(_.tileArea.tiles.foreach(t => preplacement.place(Fit(t, PreplacementTemplates.walkway))))
    With.geography.bases.foreach(b => preplacement.place(Fit(b.townHallTile, PreplacementTemplates.townhall)))
    With.geography.bases.foreach(b => b.resourcePathTiles.foreach(t => preplacement.place(Fit(t, PreplacementTemplates.walkway))))
    With.geography.zones.foreach(preplaceWalls)
    With.geography.zones.foreach(preplaceZone)
  }

  private def preplaceZone(zone: Zone): Unit = {
    val bounds        = zone.boundary
    if (bounds.cornerTilesInclusive.distinct.length < 4) return // Alchemist has a degenerate 1xN zone
    val exitDirection = zone.exit.map(_.direction).getOrElse(zone.centroid.subtract(SpecificPoints.tileMiddle).direction)
    val exitTile      = zone.exit.map(_.pixelCenter.tile).getOrElse(zone.centroid)
    val tilesFront    = bounds.cornerTilesInclusive.sortBy(t => Math.min(Math.abs(t.x - exitTile.x), Math.abs(t.y - exitTile.y))).take(2)
    val tilesBack     = bounds.cornerTilesInclusive.filterNot(tilesFront.contains)
    val cornerFront   = tilesFront.maxBy(_.tileDistanceSquared(SpecificPoints.tileMiddle))
    val cornerBack    = tilesBack.maxBy(_.tileDistanceSquared(cornerFront))

    val directionToBack   = new Direction(cornerFront, cornerBack)
    val directionToFront  = new Direction(cornerBack, cornerFront)

    fits ++= preplacement.fit     (exitTile,    bounds, directionToBack,  PreplacementTemplates.batterycannon)
    fits ++= preplacement.fitAny  (cornerFront, bounds, directionToBack,  PreplacementTemplates.initialLayouts)
    fits ++= preplacement.fitAny  (cornerFront, bounds, directionToBack,  PreplacementTemplates.gateways)
    fits ++= preplacement.fitAny  (cornerBack,  bounds, directionToFront, PreplacementTemplates.tech)
    fits ++= preplacement.fitAny  (cornerFront, bounds, directionToBack,  PreplacementTemplates.gateways, 2)
    fits ++= preplacement.fitAny  (cornerBack,  bounds, directionToFront, PreplacementTemplates.tech)
    fits ++= preplacement.fitAny  (cornerFront, bounds, directionToBack,  PreplacementTemplates.gateways, 5)
  }

  private def preplaceWalls(zone: Zone): Unit = {
    // DISABLED. Terran walls are too slow to use as-is
    if (With.self.isTerran && false) {
      val terranWall = PreplaceTerranWall(zone)
      terranWall.foreach(fits.+=)
      terranWall.foreach(preplacement.place)
    }
  }
}
