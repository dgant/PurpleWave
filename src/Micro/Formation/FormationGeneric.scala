package Micro.Formation

import Information.Geography.Pathfinding.PathfindProfile
import Lifecycle.With
import Mathematics.Points.{Pixel, SpecificPoints, Tile}
import Mathematics.Maff
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import ProxyBwapi.UnitTracking.UnorderedBuffer

import scala.collection.mutable

object FormationGeneric {

  def march(units: Iterable[FriendlyUnitInfo], destination: Pixel) : Formation = {
    form(units, FormationStyleMarch, destination = Some(destination))
  }

  def guard(units: Iterable[FriendlyUnitInfo], origin: Option[Pixel] = None): Formation = {
    val finalOrigin: Pixel = origin.getOrElse(Maff.optMode(units.view.map(_.agent.origin)).getOrElse(With.geography.home.center))
    val output = finalOrigin.zone.exit
      .map(exit => FormationZone(units, finalOrigin.zone, exit))
      .getOrElse(form(units, FormationStyleGuard, origin = Some(finalOrigin), destination = Some(With.scouting.threatOrigin.center)))
    output
  }

  def engage(units: Iterable[FriendlyUnitInfo], destination: Option[Pixel] = None): Formation = {
    form(units, FormationStyleEngage, destination = destination)
  }

  def disengage(units: Iterable[FriendlyUnitInfo], origin: Option[Pixel] = None, destination: Option[Pixel] = None): Formation = {
    form(units, FormationStyleDisengage, origin = origin, destination = destination)
  }

  private def form(
    unitsUnfiltered: Iterable[FriendlyUnitInfo],
    style: FormationStyle,
    origin: Option[Pixel] = None,
    destination: Option[Pixel] = None)
      : Formation = {

    val units = unitsUnfiltered.view.filter(_.unitClass.orderable)
    if (units.forall(_.flying)) return FormationEmpty

    val groundUnits           = units.filterNot(_.flying)
    lazy val modeOrigin       = origin.map(_.nearestWalkableTile).getOrElse(Maff.mode(units.view.map(_.agent.defaultOrigin.tile)))
    lazy val modeTarget       = Maff.mode(units.view.map(u => Maff.minBy(u.matchups.targets)(u.pixelDistanceEdge).map(_.tile).getOrElse(u.agent.destination.tile)))
    lazy val vanguardUnits    = Maff.takePercentile(0.5, groundUnits)(Ordering.by(_.pixelDistanceTravelling(modeTarget)))
    lazy val centroid         = Maff.weightedExemplar(vanguardUnits.view.map(u => (u.pixel, u.subjectiveValue)))

    // Start flood filling!
    val inf = With.mapTileArea
    var apex = centroid
    floodOrigin             = centroid.tile
    floodGoal               = destination.map(_.nearestWalkableTile).getOrElse(With.scouting.mostBaselikeEnemyTile)
    floodStart              = floodGoal
    floodMinDistanceGoal    = - inf
    floodMaxDistanceGoal    = inf
    floodMaxThreat          = inf
    floodCostDistanceGoal   = 0
    floodCostDistanceOrigin = 0
    floodCostThreat         = 0
    floodCostVulnerability  = 0
    if (style == FormationStyleMarch) {
      val path = new PathfindProfile(floodOrigin, Some(floodGoal), employGroundDist = true).find
      if ( ! path.pathExists) return FormationEmpty
      val patht = path.tiles.get.view
      floodMaxDistanceGoal    = floodOrigin.groundTilesManhattan(floodGoal) - 4
      floodMinDistanceGoal    = floodMaxDistanceGoal - 8
      floodStart              = patht.find(_.groundTilesManhattan(floodGoal) == floodMinDistanceGoal).orElse(patht.find(_.groundTilesManhattan(floodGoal) == floodMinDistanceGoal + 1)).getOrElse(floodGoal)
      floodMaxThreat          = With.grids.enemyRangeGround.margin
      floodCostDistanceGoal   = 5
      floodCostDistanceOrigin = 1
    } else if (style == FormationStyleGuard) {
      floodOrigin             = modeOrigin
      floodGoal               = With.scouting.mostBaselikeEnemyTile
      floodStart              = destination.get.tile
      floodMinDistanceGoal    = 1
      floodCostDistanceGoal   = 5
      floodCostDistanceOrigin = 1
    } else if (style == FormationStyleEngage) {
      floodStart              = modeTarget
      floodCostVulnerability  = 125 // Testing this
      floodCostThreat         = 25
      floodCostDistanceOrigin = 1
      floodCostDistanceGoal   = 5
    } else if (style == FormationStyleDisengage) {
      floodOrigin             = modeOrigin
      floodStart              = centroid.tile
      floodCostDistanceOrigin = 1
      floodCostDistanceGoal   = 5
      floodCostThreat         = 100 // In practice we only need floodMaxThreat to enforce safe positions but this makes for efficient flood fill
      floodMaxThreat          = 0
    }

    val placements    = new mutable.HashMap[UnitInfo, Pixel]()
    val floodHorizon  = new mutable.PriorityQueue[(Tile, Int)]()(Ordering.by(-_._2))
    val explored      = With.grids.disposableBoolean()
    val unplaced = groundUnits
      .groupBy(_.unitClass)
      .map(g => (new UnorderedBuffer[FriendlyUnitInfo](g._2), Math.max(1, g._2.map(_.formationRange.toInt / 32).max)))
      .toVector
      .sortBy(_._2)
    floodHorizon += ((floodStart, cost(floodStart)))
    floodHorizon.foreach(tile => explored.set(tile._1, true))
    while (floodHorizon.nonEmpty && unplaced.exists(_._1.nonEmpty) ) {
      val tile = floodHorizon.dequeue()._1
      lazy val distanceTileToGoal = tile.groundTilesManhattan(floodGoal)
      if (tile.walkable
          && (floodMinDistanceGoal  <= 0    || floodMinDistanceGoal  <= distanceTileToGoal)
          && (floodMaxDistanceGoal  >= inf  || floodMaxDistanceGoal  >= distanceTileToGoal)
          && (floodMaxThreat        >= inf  || floodMaxThreat        >= With.grids.enemyRangeGround(tile))
          && (With.grids.units(tile).forall(u => u.flying || (u.isFriendly && ! u.unitClass.isBuilding)))) {
        val pixel = tile.center
        val group = unplaced.find(_._1.nonEmpty).get
        val unit = group._1.minBy(_.pixelDistanceSquared(pixel))
        group._1.remove(unit)
        placements(unit) = pixel
      }
      val neighbors = tile
        .adjacent4
        .view
        .filter(_.valid)
        .filterNot(explored.get)
        .map(tile => (tile, cost(tile)))
      floodHorizon ++= neighbors
      neighbors.foreach(neighbor => explored.set(neighbor._1, true))
    }
    lazy val groundPlacementCentroid = Maff.centroid(placements.values)
    units
      .filter(_.flying)
      .foreach(u => placements(u) = floodOrigin.center.project(floodStart.center, Math.max(0, floodOrigin.center.pixelDistance(floodStart.center) - u.formationRange)))
    new Formation(style, placements.toMap)
  }

  private var floodOrigin             = SpecificPoints.tileMiddle
  private var floodGoal               = SpecificPoints.tileMiddle
  private var floodStart              = SpecificPoints.tileMiddle
  private var floodMaxDistanceGoal    = With.mapTileArea
  private var floodMinDistanceGoal    = - With.mapTileArea
  private var floodMaxThreat          = With.mapTileArea
  private var floodCostDistanceGoal   = 0
  private var floodCostDistanceOrigin = 0
  private var floodCostThreat         = 0
  private var floodCostVulnerability  = 0
  @inline private final def vGrid = With.grids.enemyVulnerabilityGround
  private final def cost(tile: Tile): Int = {
    if ( ! tile.walkable) return With.mapPixelPerimeter
    val costDistanceGoal    = if (floodCostDistanceGoal == 0)   0 else floodCostDistanceGoal    * tile.groundTilesManhattan(floodGoal)
    val costDistanceOrigin  = if (floodCostDistanceOrigin == 0) 0 else floodCostDistanceOrigin  * tile.groundTilesManhattan(floodOrigin)
    val costThreat          = if (floodCostThreat == 0)         0 else floodCostThreat          * With.grids.enemyRangeGround(tile)
    val costVulnerability   = if (floodCostVulnerability == 0)  0 else floodCostVulnerability   * Math.max(0, vGrid.margin + vGrid.maxVulnerability - vGrid(tile))
    // TODO: The vulnerability cost should vary based on the range of the unit.
    // Punishing, eg, a dragoon for not being adjacent to its target, doesn't allow sniping units from uphill
    // Our current formula doesn't allow this, as we do a single flood-fill across unit types
    costDistanceGoal + costDistanceOrigin + costThreat + costVulnerability
  }
}
