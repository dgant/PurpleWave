package Placement

import Information.Geography.Types.Zone
import Lifecycle.With
import Mathematics.Points.{Direction, Points, Tile}
import Placement.Generation.{Fit, Fitter, TemplatesGeneric, TemplatesProtoss, TemplatesTerran}
import Placement.Walls.{FindWall, Wall, WallFinder}
import Utilities.?

import scala.collection.mutable.ArrayBuffer

class Placement extends Fitter {

  private var _initialized: Boolean = false

  val wallFinders: ArrayBuffer[WallFinder] = new ArrayBuffer[WallFinder]()
  val wallEverything                       = false

  def wall: Option[Wall] = wallFinders.headOption.flatMap(_.wall)

  private def protoss (todo: => Unit): Unit = { if (With.self.isProtoss)  { todo }}
  private def terran  (todo: => Unit): Unit = { if (With.self.isTerran)   { todo }}
  private def zerg    (todo: => Unit): Unit = { if (With.self.isZerg)     { todo }}

  def initialize(): Unit = {
    if (_initialized) return
    _initialized = true

    // Place walkways under neutral buildings
    With.units.neutral
      .filter(_.unitClass.isBuilding)
      .filterNot(u => u.base.exists(_.townHallArea.intersects(u.tileArea)))
      .foreach(_.tileArea.tiles.map(Fit(_, TemplatesGeneric.walkway)).foreach(index))

    // Fit closer zones first, so in case of tiebreaks we prefer closer zones
    val basesSorted = With.geography.bases.sortBy(b => b.heart.groundTiles(With.geography.home))
    val zonesSorted = With.geography.zones.sortBy(z => z.heart.groundTiles(With.geography.home))

    basesSorted.foreach(b => index(Fit(b.townHallTile, TemplatesGeneric.townhall)))
    if (wallEverything) {
      With.geography.zones
        .filter(_.bases.exists(b => b.isNatural || With.geography.ourFoyer == b))
        .sortBy( ! _.metro.exists(_.isOurs))
        .foreach(preplaceWalls)
      //zonesSorted.sortBy(_.heart.groundTiles(With.geography.home)).foreach(preplaceWalls)
    } else if ( ! With.self.isProtoss || With.enemies.exists( ! _.isProtoss)) {
      preplaceWalls(With.geography.ourFoyer.zone)
    }
    basesSorted.foreach(base => {
      protoss (fitAndIndexConstrained(5, 1, ?(base.isMain, TemplatesProtoss.mainBases, TemplatesProtoss.bases), base))
      terran  (fitAndIndexConstrained(0, 1, TemplatesTerran.bases, base))
    })
    basesSorted.foreach(_.resourcePathTiles.foreach(t => if (at(t).requirement.buildableAfter) index(Fit(t, TemplatesGeneric.walkway))))
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
    if ( zone.tiles.contains(Points.tileMiddle)) {
      protoss (fitAndIndexProximity(1, 1, TemplatesProtoss.proxy4,  Points.tileMiddle.walkableTile, zone))
      protoss (fitAndIndexProximity(1, 1, TemplatesProtoss.proxy2,  Points.tileMiddle.walkableTile, zone))
      terran  (fitAndIndexProximity(1, 1, TemplatesTerran.proxy2,   Points.tileMiddle.walkableTile, zone))
    }
    if ( ! zone.isBackyard && ! zone.island) {
      protoss (fitAndIndexProximity(1, 1, TemplatesProtoss.batterycannon, exitTile, zone, 10))
      terran  (fitAndIndexProximity(1, 1, TemplatesTerran.bunkerTurret,   exitTile, zone, 10))
    }
    protoss (fitAndIndexProximity(0, 1, TemplatesProtoss.initialLayouts,  zone.downtown,  zone))
    protoss (fitAndIndexRectangle(2, 1, TemplatesProtoss.tech,            cornerBack,     bounds, directionToFront))
    protoss (fitAndIndexRectangle(3, 1, TemplatesProtoss.production,      cornerFront,    bounds, directionToBack))
    protoss (fitAndIndexRectangle(4, 1, TemplatesProtoss.tech,            cornerBack,     bounds, directionToFront))
    protoss (fitAndIndexRectangle(4, 7, TemplatesProtoss.production,      cornerFront,    bounds, directionToBack))
    terran  (fitAndIndexProximity(1, 1, TemplatesTerran.initialLayouts,   zone.downtown,  zone))
    terran  (fitAndIndexRectangle(2, 1, TemplatesTerran.production,       cornerFront,    bounds, directionToBack))
    terran  (fitAndIndexRectangle(3, 1, TemplatesTerran.supply,           cornerBack,     bounds, directionToFront))
    terran  (fitAndIndexRectangle(4, 1, TemplatesTerran.tech6,            cornerBack,     bounds, directionToFront))
    terran  (fitAndIndexRectangle(5, 2, TemplatesTerran.supply,           cornerBack,     bounds, directionToFront))
    terran  (fitAndIndexRectangle(6, 3, TemplatesTerran.production,       cornerFront,    bounds, directionToBack))
    terran  (fitAndIndexRectangle(7, 3, TemplatesTerran.supply,           cornerBack,     bounds, directionToFront))
    terran  (fitAndIndexRectangle(8, 7, TemplatesTerran.production,       cornerFront,    bounds, directionToBack))
  }

  def preplaceWalls(zone: Zone): Unit = {
    val wallFinder = FindWall(zone)
    wallFinder.flatMap(_.wall).map(_.toFit).foreach(index)
    wallFinders ++= wallFinder
  }

  def auditWall(tiles: Tile*): Unit = {
    val matching =
      ( wallFinders.flatMap(_.wallsAcceptable) ++
        wallFinders.flatMap(_.wallsUnacceptable))
      .filter(w => tiles.forall(t => w.buildings.exists(b => b._1 == t)))
    matching.foreach(w => With.logger.debug(w.toString))
  }
}
