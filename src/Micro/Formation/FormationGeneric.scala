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
    //form(group, FormationStyleEngage, face = finalTowards, approach = finalTowards)
    form(group, FormationStyleMarch, face = finalTowards, approach = finalTowards)
  }

  def disengage(group: FriendlyUnitGroup, towards: Option[Pixel] = None): Formation = {
    form(group, FormationStyleDisengage, face = targetPixel(group), approach = towards.getOrElse(group.homeConsensus))
  }

  private def targetPixel(group: FriendlyUnitGroup): Pixel = {
    Maff.modeOpt(group.groupFriendlyOrderable.view.flatMap(u => Maff.minBy(u.matchups.targets.view.map(_.pixel))(u.pixelDistanceCenter))).getOrElse(With.scouting.threatOrigin.center)
  }

  private case class ClassSlots(unitClass: UnitClass, var slots: Int)

  private val inf = LightYear()
  private def form(group: FriendlyUnitGroup, style: FormationStyle, face: Pixel, approach: Pixel): Formation = {
    val units = group.groupFriendlyOrderable
    val groundUnits = units.filterNot(_.flying)
    if (groundUnits.isEmpty) return FormationEmpty

    lazy val vanguardUnits  = Maff.takePercentile(0.5, groundUnits)(Ordering.by(_.pixelDistanceTravelling(face)))
    lazy val centroid       = Maff.weightedExemplar(vanguardUnits.view.map(u => (u.pixel, u.subjectiveValue)))
    lazy val path           = new PathfindProfile(floodCentroid, Some(floodGoal), lengthMaximum = Some(20), employGroundDist = true, costImmobility = 1.5).find
    lazy val patht          = path.tiles.get.view

    floodCentroid           = centroid.nearestWalkableTile
    floodGoal               = approach.nearestWalkableTile
    floodApex               = floodGoal
    floodMinDistanceTarget  = - inf
    floodMaxDistanceTarget  = inf
    floodMaxThreat          = inf
    floodCostDistanceGoal   = 0
    floodCostDistanceApex   = 0
    floodCostThreat         = 0
    floodCostVulnerability  = 0
    if (style == FormationStyleMarch || style == FormationStyleDisengage) {
      if ( ! path.pathExists) return FormationEmpty
      var stepSizeTiles = Seq(4.0,
        if (style == FormationStyleDisengage) 5.0 + floodCentroid.enemyRange else 0.0,
        With.reaction.estimationAverage * group.meanTopSpeed / 32.0 + 0.5).max.toInt
      if (style == FormationStyleDisengage) {
        stepSizeTiles = Math.max(stepSizeTiles, 2 + Maff.max(units.view.map(_.matchups.pixelsOfEntanglement.toInt)).getOrElse(0) / 32)
      }
      floodMaxDistanceTarget  = floodCentroid.tileDistanceGroundManhattan(floodGoal) - 1
      floodMinDistanceTarget  = floodMaxDistanceTarget - stepSizeTiles
      floodApex               = patht.reverseIterator.filterNot(t => t.zone.edges.exists(_.contains(t.center))).find(_.tileDistanceGroundManhattan(floodGoal) >= floodMinDistanceTarget).orElse(patht.find(_.tileDistanceGroundManhattan(floodGoal) == floodMinDistanceTarget + 1)).getOrElse(floodGoal)
      floodMaxThreat          = if (style == FormationStyleMarch) With.grids.enemyRangeGround.margin else With.grids.enemyRangeGround.defaultValue
      floodCostDistanceGoal   = 5
      floodCostDistanceApex   = 1
    } else if (style == FormationStyleGuard) {
      floodMinDistanceTarget  = 1
      floodCostDistanceGoal   = 5
      floodCostDistanceApex   = 1
    } else if (style == FormationStyleEngage) {
      floodGoal               = floodCentroid
      floodCostVulnerability  = 1
      floodCostThreat         = 1
      floodCostDistanceApex   = 1
      floodCostDistanceGoal   = 1
    }

    val slots         = new mutable.HashMap[UnitClass, ArrayBuffer[Pixel]]()
    val floodHorizon  = new mutable.PriorityQueue[(Tile, Int)]()(Ordering.by(-_._2))
    val explored      = With.grids.disposableBoolean()
    val unplaced      = groundUnits
      .groupBy(_.unitClass)
      .map(g => (ClassSlots(g._1, g._2.size), g._2.head.formationRangePixels / 32))
      .toVector
      .sortBy(_._2)
    floodHorizon += ((floodApex, cost(floodApex)))
    floodHorizon.foreach(tile => explored.set(tile._1, true))
    while (floodHorizon.nonEmpty && unplaced.exists(_._1.slots > 0) ) {
      val tile = floodHorizon.dequeue()._1
      lazy val distanceTileToGoal = tile.tileDistanceGroundManhattan(floodGoal)
      if (tile.walkable
          && (floodMinDistanceTarget  <= 0    || floodMinDistanceTarget <= distanceTileToGoal)
          && (floodMaxDistanceTarget  >= inf  || floodMaxDistanceTarget >= distanceTileToGoal)
          && (floodMaxThreat >= tile.enemyRangeGroundUnchecked)
          && (tile.units.forall(u => u.flying || (u.isFriendly && ! u.unitClass.isBuilding)))) {
        val unplacedClass = unplaced.find(_._1.slots > 0).get

        // To avoid diving with Engage formations:
        // If we care about enemy vulnerability, position units only as close as is necessary to hit something
        // Allow pushing through chokes so we can actually take fights there if necessary
        if (
          floodCostVulnerability == 0
          || tile.enemyRangeGround == 0
          || unplacedClass._2 < (vGrid.margin + vGrid.maxVulnerability - vGrid(tile))) {
          val classSlot = unplacedClass._1
          classSlot.slots -= 1
          if (!slots.contains(classSlot.unitClass)) {
            slots(classSlot.unitClass) = ArrayBuffer.empty
          }
          slots(classSlot.unitClass) += tile.center
        }
      }
      val neighbors = tile
        .adjacent4
        .view
        .filter(_.valid)
        .filterNot(explored.get)
        .filter(floodMaxDistanceTarget >= _.tileDistanceGroundManhattan(floodGoal))
        .map(tile => (tile, cost(tile)))
      floodHorizon ++= neighbors
      neighbors.foreach(neighbor => explored.set(neighbor._1, true))
    }

    lazy val groundPlacementCentroid = Maff.exemplarOption(slots.values.view.flatten).getOrElse(floodApex.center)
    units
      .filter(_.flying)
      .foreach(u => {
        if ( ! slots.contains(u.unitClass)) {
          slots(u.unitClass) = ArrayBuffer.empty
        }
        slots(u.unitClass) += groundPlacementCentroid
      })
    val unassigned = UnassignedFormation(style, slots.toMap, group)
    //val output = if (style == FormationStyleGuard || style == FormationStyleEngage) unassigned.outwardFromCentroid else unassigned.sprayToward(approach)
    val output = unassigned.outwardFromCentroid
    if (style == FormationStyleMarch || style == FormationStyleDisengage) { output.path = Some(path) }
    output
  }

  private var floodCentroid           = SpecificPoints.tileMiddle
  private var floodGoal               = SpecificPoints.tileMiddle
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
    val costDistanceGoal    = if (floodCostDistanceGoal == 0)   0 else floodCostDistanceGoal  * tile.tileDistanceGroundManhattan(floodGoal)
    val costDistanceOrigin  = if (floodCostDistanceApex == 0)   0 else floodCostDistanceApex  * tile.tileDistanceGroundManhattan(floodCentroid)
    val costThreat          = if (floodCostThreat == 0)         0 else floodCostThreat        * tile.enemyRangeGround
    val costVulnerability   = if (floodCostVulnerability == 0)  0 else floodCostVulnerability * Maff.fromBoolean(vGrid(tile) == 0)
    costDistanceGoal + costDistanceOrigin + costThreat + costVulnerability
  }
}
