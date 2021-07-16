package Micro.Formation

import Information.Geography.Pathfinding.PathfindProfile
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{Pixel, SpecificPoints, Tile}
import ProxyBwapi.UnitClasses.UnitClass
import Tactics.Squads.FriendlyUnitGroup
import Utilities.LightYear

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object FormationGeneric {
  def march(group: FriendlyUnitGroup, destination: Pixel) : Formation = {
    form(group, FormationStyleMarch, face = destination, approach = destination)
  }

  def guard(group: FriendlyUnitGroup, toGuard: Option[Pixel]): Formation = {
    val guardZone = toGuard.getOrElse(group.homeConsensus).zone
    guardZone.exit
      .map(exit => FormationZone(group, guardZone, exit))
      .getOrElse(form(group, FormationStyleGuard, face = targetPixel(group), approach = group.centroidKey))
  }

  def engage(group: FriendlyUnitGroup, towards: Option[Pixel]): Formation = {
    val finalTowards = towards.getOrElse(targetPixel(group))
    form(group, FormationStyleEngage, face = finalTowards, approach = finalTowards)
  }

  def disengage(group: FriendlyUnitGroup): Formation = {
    form(group, FormationStyleDisengage, face = targetPixel(group), approach = group.homeConsensus)
  }

  private def targetPixel(group: FriendlyUnitGroup): Pixel = {
    Maff.mode(group.groupFriendlyOrderable.view.map(u =>
      Maff.minBy(u.matchups.targets.view.map(_.pixel))(u.pixelDistanceCenter).getOrElse(With.scouting.threatOrigin.center)))
  }

  private case class ClassSlots(unitClass: UnitClass, var slots: Int)

  private val inf = LightYear()
  private def form(group: FriendlyUnitGroup, style: FormationStyle, face: Pixel, approach: Pixel): Formation = {
    val units = group.groupFriendlyOrderable
    val groundUnits = units.filterNot(_.flying)
    if (groundUnits.isEmpty) return FormationEmpty

    lazy val vanguardUnits  = Maff.takePercentile(0.5, groundUnits)(Ordering.by(_.pixelDistanceTravelling(face)))
    lazy val centroid       = Maff.weightedExemplar(vanguardUnits.view.map(u => (u.pixel, u.subjectiveValue)))
    lazy val path           = new PathfindProfile(floodCentroid, Some(floodTarget), employGroundDist = true).find
    lazy val patht          = path.tiles.get.view

    floodCentroid           = centroid.tile
    floodTarget             = approach.nearestWalkableTile
    floodApex               = floodTarget
    floodMinDistanceTarget  = - inf
    floodMaxDistanceTarget  = inf
    floodMaxThreat          = inf
    floodCostDistanceGoal   = 0
    floodCostDistanceApex   = 0
    floodCostThreat         = 0
    floodCostVulnerability  = 0
    if (style == FormationStyleMarch || style == FormationStyleDisengage) {
      if ( ! path.pathExists) return FormationEmpty
      val stepSizeTiles = Seq(3.0,
        if (style == FormationStyleDisengage) 2.0 + floodCentroid.enemyRange else 0.0,
        With.reaction.estimationAverage * group.meanTopSpeed / 32.0 + 0.5).max.toInt
      floodMaxDistanceTarget  = floodCentroid.tileDistanceGroundManhattan(floodTarget) - 1
      floodMinDistanceTarget  = floodMaxDistanceTarget - stepSizeTiles
      floodApex               = patht.find(_.tileDistanceGroundManhattan(floodTarget) == floodMinDistanceTarget).orElse(patht.find(_.tileDistanceGroundManhattan(floodTarget) == floodMinDistanceTarget + 1)).getOrElse(floodTarget)
      floodMaxThreat          = if (style == FormationStyleMarch) With.grids.enemyRangeGround.margin else With.grids.enemyRangeGround.defaultValue
      floodCostDistanceGoal   = 5
      floodCostDistanceApex   = 1
    } else if (style == FormationStyleGuard) {
      floodMinDistanceTarget  = 1
      floodCostDistanceGoal   = 5
      floodCostDistanceApex   = 1
    } else if (style == FormationStyleEngage) {
      floodCostVulnerability  = 125
      floodCostThreat         = 25
      floodCostDistanceApex   = 1
      floodCostDistanceGoal   = 5
    }

    val slots         = new mutable.HashMap[UnitClass, ArrayBuffer[Pixel]]()
    val floodHorizon  = new mutable.PriorityQueue[(Tile, Int)]()(Ordering.by(-_._2))
    val explored      = With.grids.disposableBoolean()
    val unplaced      = groundUnits
      .groupBy(_.unitClass)
      .map(g => (ClassSlots(g._1, g._2.size), g._2.head.formationRange))
      .toVector
      .sortBy(_._2)
    floodHorizon += ((floodApex, cost(floodApex)))
    floodHorizon.foreach(tile => explored.set(tile._1, true))
    while (floodHorizon.nonEmpty && unplaced.exists(_._1.slots > 0) ) {
      val tile = floodHorizon.dequeue()._1
      lazy val distanceTileToGoal = tile.tileDistanceGroundManhattan(floodTarget)
      if (tile.walkable
          && (floodMinDistanceTarget  <= 0    || floodMinDistanceTarget  <= distanceTileToGoal)
          && (floodMaxDistanceTarget  >= inf  || floodMaxDistanceTarget  >= distanceTileToGoal)
          && (floodMaxThreat          >= inf  || floodMaxThreat        >= tile.enemyRangeGround)
          && (tile.units.forall(u => u.flying || (u.isFriendly && ! u.unitClass.isBuilding)))) {
        val pixel = tile.center
        val classSlot = unplaced.find(_._1.slots > 0).get._1
        classSlot.slots -= 1
        if ( ! slots.contains(classSlot.unitClass)) {
          slots(classSlot.unitClass) = ArrayBuffer.empty
        }
        slots(classSlot.unitClass) += pixel
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

    lazy val groundPlacementCentroid = Maff.centroid(slots.values.view.flatten)
    units
      .filter(_.flying)
      .foreach(u => {
        if ( ! slots.contains(u.unitClass)) {
          slots(u.unitClass) = ArrayBuffer.empty
        }
        slots(u.unitClass) += floodCentroid.center.project(floodApex.center, Math.max(0, floodCentroid.center.pixelDistance(floodApex.center) - u.formationRange))
      })
    val unassigned = UnassignedFormation(style, slots.toMap, group)
    val output = if (style == FormationStyleGuard || style == FormationStyleEngage) unassigned.outwardFromCentroid else unassigned.sprayToward(approach)
    output
  }

  private var floodCentroid           = SpecificPoints.tileMiddle
  private var floodTarget             = SpecificPoints.tileMiddle
  private var floodApex               = SpecificPoints.tileMiddle
  private var floodMaxDistanceTarget  = inf
  private var floodMinDistanceTarget  = - inf
  private var floodMaxThreat          = inf
  private var floodCostDistanceGoal   = 0
  private var floodCostDistanceApex   = 0
  private var floodCostThreat         = 0
  private var floodCostVulnerability  = 0
  @inline private final def vGrid = With.grids.enemyVulnerabilityGround
  private final def cost(tile: Tile): Int = {
    if ( ! tile.walkable) return inf
    val costDistanceGoal    = if (floodCostDistanceGoal == 0)   0 else floodCostDistanceGoal  * tile.tileDistanceGroundManhattan(floodTarget)
    val costDistanceOrigin  = if (floodCostDistanceApex == 0)   0 else floodCostDistanceApex  * tile.tileDistanceGroundManhattan(floodCentroid)
    val costThreat          = if (floodCostThreat == 0)         0 else floodCostThreat        * tile.enemyRangeGround
    val costVulnerability   = if (floodCostVulnerability == 0)  0 else floodCostVulnerability * Math.max(0, vGrid.margin + vGrid.maxVulnerability - vGrid(tile))
    // TODO: The vulnerability cost should vary based on the range of the unit.
    // Punishing, eg, a dragoon for not being adjacent to its target, doesn't allow sniping units from uphill
    // Our current formula doesn't allow this, as we do a single flood-fill across unit types
    costDistanceGoal + costDistanceOrigin + costThreat + costVulnerability
  }
}
