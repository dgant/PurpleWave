package Placement

import Information.Geography.Types.{Base, Zone}
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{Direction, Points, Tile}
import Placement.Generation._
import Placement.Walls.{FindWall, Wall, WallFinder}
import Utilities.?

import scala.collection.mutable.ArrayBuffer

class Placement extends Fitter {

  private var _initialized: Boolean = false

  val wallFinders: ArrayBuffer[WallFinder] = new ArrayBuffer[WallFinder]()
  val wallEverything                       = false

  val forwardProxyNaturals = new ArrayBuffer[Base]()
  val cannonRushes         = new ArrayBuffer[CannonRushPlacement]()

  def wall: Option[Wall] = wallFinders.headOption.flatMap(_.wall)

  private def protoss (todo: => Unit): Unit = { if (With.self.isProtoss)  { todo }}
  private def terran  (todo: => Unit): Unit = { if (With.self.isTerran)   { todo }}
  private def zerg    (todo: => Unit): Unit = { if (With.self.isZerg)     { todo }}

  def initialize(): Unit = {
    if (_initialized) return
    _initialized = true
    forwardProxyNaturals ++= With.geography.bases.filter(b => ! b.isBackyard && b.naturalOf.exists(n => ! n.isOurs && n.exitOriginal.isDefined))

    // Place walkways under neutral buildings
    With.units.neutral
      .filter(_.unitClass.isBuilding)
      .filterNot(u => u.base.exists(_.townHallArea.intersects(u.tileArea)))
      .foreach(_.tileArea.tiles.map(Fit(_, TemplatesGeneric.walkway)).foreach(index))

    // Fit closer zones first, so in case of tiebreaks we prefer closer zones
    val basesSorted = With.geography.bases.sortBy(b => b.heart.groundTiles(With.geography.home))
    val zonesSorted = With.geography.zones.sortBy(z => z.heart.groundTiles(With.geography.home))

    basesSorted.foreach(b => {
      // For any center base, place proxies here first.
      if (b.tiles.contains(Points.tileMiddle)) {
        fitProxyGate(b.zone)
      }
      index(Fit(b.townHallTile, TemplatesGeneric.townhall))
    })
    forwardProxyNaturals --= forwardProxyNaturals.filter(fitProxyForwardRobo)
    protoss {
      cannonRushes ++= basesSorted
        .filter(base => (base.isMain || base.naturalOf.isDefined) && ! base.isOurMain && ! base.isOurNatural)
        .flatMap(CannonRushFinder.find(_, this))
      cannonRushes.foreach(rush => index(rush.fit))
    }
    if (wallEverything) {
      With.geography.zones
        .filter(_.bases.exists(b => b.isNatural || With.geography.ourFoyer == b))
        .sortBy( ! _.metro.exists(_.isOurs))
        .foreach(fitWall)
    } else if (With.self.isProtoss) {
      fitWall(With.geography.ourFoyer.zone)
    }
    basesSorted.foreach(base => {
      protoss (fitAndIndexConstrained(5, 1, ?(base.isOurMain, TemplatesProtoss.mainBases, TemplatesProtoss.expansions), base))
      terran  (fitAndIndexConstrained(0, 1, TemplatesTerran.bases,                                                      base))
      zerg    (fitAndIndexConstrained(0, 1, ?(base.isOurMain, TemplatesZerg.mainBases,    TemplatesZerg.expansions),    base))
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
    val canProxyGateHere  = zone.metro.forall(_.main.isEmpty) &&
      Maff.min(With.geography.startLocations
        .filterNot(With.self.startTile==)
        .map(_.groundTiles(zone.heart)))
      .exists(_ < With.strategy.rushDistanceMin * 0.7)

    if (canProxyGateHere) {
      fitProxyGate(zone)
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
    terran  (fitAndIndexRectangle(5, 1, TemplatesTerran.production,       cornerFront,    bounds, directionToBack))
    terran  (fitAndIndexRectangle(6, 1, TemplatesTerran.supply,           cornerBack,     bounds, directionToFront))
    terran  (fitAndIndexRectangle(7, 4, TemplatesTerran.production,       cornerFront,    bounds, directionToBack))
    terran  (fitAndIndexRectangle(8, 4, TemplatesTerran.supply,           cornerBack,     bounds, directionToFront))
    zerg    (fitAndIndexProximity(2, 3, TemplatesZerg.tech,               zone.heart,     zone))
    zerg    (fitAndIndexRectangle(2, 8, TemplatesZerg.macroHatch,         cornerFront,    bounds, directionToBack))
  }

  private def fitProxyForwardRobo(natural: Base): Boolean = {
    if ( ! With.self.isProtoss) return false
    if ( ! With.enemies.exists(_.isProtoss)) return false
    val mainHeart     = natural.naturalOf.get.heart
    val naturalHeart  = natural.heart
    val exit          = natural.exitOriginal.get.pixelCenter.walkableTile
    val exitDistance  = exit.groundTiles(mainHeart)
    val tiles         = With.geography.zones.flatMap(_.tiles).distinct
    val outwardX      = exit.x - naturalHeart.x
    val outwardY      = exit.y - naturalHeart.y

    val candidates = tiles
      .filter(t =>
        Maff.betweenI(t.groundTiles(exit), 13, 30)
        && t.groundTiles(mainHeart) > exitDistance
        && (t.x - exit.x) * outwardX + (t.y - exit.y) * outwardY > 0)
      .sortBy(t => (t.groundTiles(exit), t.tileDistanceSquared(exit)))
    val fit = candidates.view.flatMap(candidate =>
      TemplatesProtoss.proxyForwardRobo.view.map(template => {
        val robo = template.points.find(p => p.requirement.width == 3 && p.requirement.height == 2).get
        val origin = candidate.subtract(robo.point.x + 1, robo.point.y + 1)
        (template, origin)
      }))
      .find(f => fitsAt(f._1, f._2))
    fit.foreach(f => index(Fit(f._2, f._1, 1)))
    fit.nonEmpty
  }

  def fitProxyGate(zone: Zone): Unit = {
    protoss {
      val proxyBaseFit = zone.bases.find(_.tiles.contains(Points.tileMiddle)).map(fitAndIndexConstrained(1, 1, TemplatesProtoss.proxyBase, _)).getOrElse(Seq.empty)
      if (proxyBaseFit.isEmpty) {
        fitAndIndexProximity(1, 1, TemplatesProtoss.proxy4, zone.centroid, zone)
        fitAndIndexProximity(1, 1, TemplatesProtoss.proxy2, zone.centroid, zone)
      }
    }
    terran  (fitAndIndexProximity(1, 1, TemplatesTerran.proxy2,   zone.centroid, zone))
  }

  def fitWall(zone: Zone): Unit = {
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
