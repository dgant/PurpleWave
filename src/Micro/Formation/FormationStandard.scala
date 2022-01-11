package Micro.Formation

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Information.Geography.Pathfinding.PathfindProfile
import Information.Grids.Floody.GridEnemyVulnerabilityGround
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{Pixel, PixelRay, Tile}
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Tactics.Squads.FriendlyUnitGroup
import Utilities.LightYear

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class FormationStandard(val group: FriendlyUnitGroup, val style: FormationStyle, val goal: Pixel) extends Formation {
  private case class ClassSlots(unitClass: UnitClass, var slots: Int, formationRangePixels: Double)

  private val units = group.groupFriendlyOrderable
  private val airUnits = units.filter(_.flying)
  private val groundUnits = units.filterNot(_.flying)

  val targetsAll        = group.groupUnits.flatMap(_.battle).distinct.flatMap(_.enemy.units).filter(e => group.groupUnits.exists(_.canAttack(e)))
  val targetsTowards    = targetsAll.filter(_.pixelDistanceTravelling(goal) < group.centroidGround.groundPixels(goal))
  val targetAll         = Maff.minBy(targetsAll)(_.pixelDistanceTravelling(group.centroidGround))
  val targetTowards     = Maff.minBy(targetsTowards)(_.pixelDistanceTravelling(group.centroidGround))
  val target            = targetTowards.orElse(targetAll).map(_.pixel).getOrElse(With.scouting.threatOrigin.center)
  val vanguardOrigin    = if (style == FormationStyleEngage || style == FormationStyleDisengage) target else goal
  val vanguardUnits     = Maff.takePercentile(0.5, groundUnits)(Ordering.by(_.pixelDistanceTravelling(vanguardOrigin)))
  var centroid          = Maff.weightedExemplar(vanguardUnits.view.map(u => (u.pixel, u.subjectiveValue))).walkablePixel
  val goalPath          = new PathfindProfile(centroid.walkableTile, Some(goal.walkableTile),   lengthMaximum = Some(20), employGroundDist = true, costImmobility = 1.5).find
  val targetPath        = new PathfindProfile(centroid.walkableTile, Some(target.walkableTile), lengthMaximum = Some(10), employGroundDist = true, costImmobility = 1.5).find
  val goalPathTiles     = goalPath.tiles.get.view
  val groupWidthPixels  = groundUnits.map(_.unitClass.dimensionMax).sum
  val face              = if (style == FormationStyleEngage || style == FormationStyleMarch) {
    targetTowards.map(_.pixel).getOrElse(goal)
  } else {
    targetTowards.orElse(targetAll).map(_.pixel).orElse(targetPath.tiles.map(p => p.find( ! _.visible).getOrElse(p.last).center)).getOrElse(target)
  }
  var apex              = goal
  var minTilesToGoal    = - LightYear()
  var maxTilesToGoal    = LightYear()
  var maxThreat         = LightYear()
  var costDistanceGoal  = 0
  var costDistanceApex  = 0
  var costThreat        = 0
  var costVulnerability = 0
  parameterize()
  val slots: Map[UnitClass, Seq[Pixel]] = slotsByClass()
  val placements: Map[FriendlyUnitInfo, Pixel] = UnassignedFormation(style, slots, group).outwardFromCentroid

  private def parameterize(): Unit = {
    if (style == FormationStyleMarch || style == FormationStyleDisengage) {
      if (goalPath.pathExists) {
        val pixelsToEngage = units.view.map(u => Math.min(
          Maff.min(u.matchups.threats.map(_.pixelsToGetInRange(u))).getOrElse(1024.0),
          Maff.min(u.matchups.targets.map(u.pixelsToGetInRange)).getOrElse(1024.0))).min
        var stepSizeTiles = Math.max(
            Maff.clamp(pixelsToEngage / 32, 2, 4),
            With.reaction.estimationAverage * group.meanTopSpeed / 32.0 + 1)
          .toInt
        if (style == FormationStyleDisengage) {
          stepSizeTiles = Math.max(stepSizeTiles, 2 + Maff.max(units.view.map(_.matchups.pixelsOfEntanglement.toInt)).getOrElse(0) / 32)
          stepSizeTiles = Math.max(stepSizeTiles, 10 + 2 * centroid.tile.enemyRange)
        }
        maxTilesToGoal    = centroid.tile.groundTiles(goal) - 1
        minTilesToGoal    = maxTilesToGoal - stepSizeTiles
        maxThreat         = if (style == FormationStyleMarch) With.grids.enemyRangeGround.margin else With.grids.enemyRangeGround.defaultValue
        costDistanceGoal  = 5
        costDistanceApex  = 1
        apex = goalPathTiles
          .reverseIterator
          .filterNot(t => t.zone.edges.exists(e => e.radiusPixels < groupWidthPixels / 4 && e.contains(t.center)))
          .find(_.groundTiles(goal) >= minTilesToGoal)
          .orElse(goalPathTiles.find(_.groundTiles(goal) == minTilesToGoal + 1))
          .map(_.center)
          .getOrElse(goal)
      }
    } else if (style == FormationStyleGuard) {
      minTilesToGoal    = 1
      costDistanceGoal  = 5
      costDistanceApex  = 1
    } else if (style == FormationStyleEngage) {
      costVulnerability = 1
      costThreat        = 1
      costDistanceApex  = 1
      costDistanceGoal  = 1
    }
  }

  private def slotsByClass(): Map[UnitClass, Seq[Pixel]] = {
    val classCount = groundUnits
      .groupBy(_.unitClass)
      .map(g => (ClassSlots(g._1, g._2.size, g._2.head.formationRangePixels / 32)))
      .toVector
      .sortBy(_.formationRangePixels)
    val preferArc = style == FormationStyleGuard || style == FormationStyleEngage || PixelRay(centroid, apex).forall(_.walkable)
    lazy val ourArcSlots = arcSlots(classCount, 32 + apex.pixelDistance(face))
    lazy val ourFloodSlots = floodSlots(classCount)
    var output = if (preferArc) ourArcSlots else ourFloodSlots
    if (output.isEmpty) {
      output = if (preferArc) ourFloodSlots else ourArcSlots
    }

    val groundPlacementCentroid = Maff.exemplarOption(output.values.view.flatten).getOrElse(apex)
    output ++= airUnits
      .groupBy(_.unitClass)
      .map(u => (u._1, u._2.map(x => groundPlacementCentroid)))
    output
  }

  /**
  * Position formation slots by flooding outwards from the apex
  */
  private final def vGrid: GridEnemyVulnerabilityGround = With.grids.enemyVulnerabilityGround
  private def floodSlots(classSlots: Vector[ClassSlots]): Map[UnitClass, Seq[Pixel]] = {
    val output        = new mutable.HashMap[UnitClass, ArrayBuffer[Pixel]]()
    val floodHorizon  = new mutable.PriorityQueue[(Tile, Int)]()(Ordering.by(-_._2))
    val explored      = With.grids.disposableBoolean()
    floodHorizon += ((apex.tile, cost(apex.tile)))
    floodHorizon.foreach(tile => explored.set(tile._1, true))
    while (floodHorizon.nonEmpty && classSlots.exists(_.slots > 0) ) {
      val tile = floodHorizon.dequeue()._1
      lazy val distanceTileToGoal = tile.groundTiles(goal)
      if (tile.walkable
          && (minTilesToGoal  <= 0            || minTilesToGoal <= distanceTileToGoal)
          && (maxTilesToGoal  >= LightYear()  || maxTilesToGoal >= distanceTileToGoal)
          && (maxThreat >= tile.enemyRangeGroundUnchecked)
          && (tile.units.forall(u => u.flying || (u.isFriendly && ! u.unitClass.isBuilding)))) {
        val unplacedClass = classSlots.find(_.slots > 0).get

        // To avoid diving with Engage formations:
        // If we care about enemy vulnerability, position units only as close as is necessary to hit something
        // Allow pushing through chokes so we can actually take fights there if necessary
        if (
          costVulnerability == 0
          || tile.enemyRangeGround == 0
          || unplacedClass.formationRangePixels < (vGrid.margin + vGrid.maxVulnerability - vGrid(tile))) {
          unplacedClass.slots -= 1
          if (!output.contains(unplacedClass.unitClass)) {
            output(unplacedClass.unitClass) = ArrayBuffer.empty
          }
          output(unplacedClass.unitClass) += tile.center
        }
      }
      val neighbors = tile
        .adjacent4
        .view
        .filter(_.valid)
        .filterNot(explored.get)
        .filter(maxTilesToGoal >= _.groundTiles(goal))
        .map(tile => (tile, cost(tile)))
      floodHorizon ++= neighbors
      neighbors.foreach(neighbor => explored.set(neighbor._1, true))
    }
    output.toMap
  }

  /**
    * Positions formation slots by tracing an arc around the goal, centered at the apex.
    */
  private def arcSlots(classSlots: Vector[ClassSlots], radius: Double): Map[UnitClass, Seq[Pixel]] = {
    With.grids.formationSlots.reset()
    var unitClass = Terran.Marine
    var zone = apex.zone
    val dr = 4d
    val a0 = face.radiansTo(apex)
    var r = Math.max(0, face.pixelDistance(apex) - dr)
    var da = 0d
    var i = -1
    var n = 0
    var haltNegative = false
    var haltPositive = false
    def nextRow(): Unit = {
      r += dr
      da = dr / r
      i = -1
      haltNegative = false
      haltPositive = false
    }
    nextRow()
    def proceed(): Boolean = {
      n += 1
      if (n > 10000) return false
      i += 1
      val s = if (i % 2 == 0) 1 else -1
      if (s < 0 && haltNegative) return true
      if (s > 0 && haltPositive) return true
      val m = s * i / 2
      val p = face.radiateRadians(a0 + da * m, r)
      if ( ! p.walkable) {
        if (p.zone != zone) {
          haltNegative ||= s < 0
          haltPositive ||= s > 0
          if (haltNegative && haltPositive) {
            nextRow()
          }
        }
        return true
      }
      val groundTilesToGoal = p.groundPixels(goal) / 32
      if (groundTilesToGoal > maxTilesToGoal) return false // We've gone too far and will never find a slot
      if (groundTilesToGoal < minTilesToGoal) return true // If we keep going we may find a slot
      if (p.tile.enemyRangeGround > maxThreat) return true
      ! With.grids.formationSlots.tryPlace(unitClass, p)
    }
    classSlots.foreach(classSlot => {
      unitClass = classSlot.unitClass
      (0 until classSlot.slots).foreach(i => while(proceed()) {})
    })
    With.grids.formationSlots.placed.groupBy(_._1).map(p => (p._1, p._2.map(_._2)))
  }

  private def cost(tile: Tile): Int = {
    if ( ! tile.walkable) return LightYear()
    val tCostDistanceGoal   = if (costDistanceGoal == 0)   0 else costDistanceGoal  * tile.groundTiles(goal)
    val tCostDistanceApex   = if (costDistanceApex == 0)   0 else costDistanceApex  * tile.groundTiles(centroid)
    val tCostThreat         = if (costThreat == 0)         0 else costThreat        * tile.enemyRangeGround
    val tCostVulnerability  = if (costVulnerability == 0)  0 else costVulnerability * Maff.fromBoolean(vGrid.get(tile) == 0)
    costDistanceGoal + tCostDistanceApex + tCostThreat + tCostVulnerability
  }

  override def renderMap(): Unit = {
    super.renderMap()
    goalPath.renderMap(Colors.brighten(style.color))
    targetPath.renderMap(Colors.darken(style.color))
    if (placements.nonEmpty) {
      DrawMap.label("Target", target.add(0, 16), true, style.color)
      DrawMap.label("Goal", goal, true, style.color)
      DrawMap.label("Apex", apex, true, style.color)
      DrawMap.label(style.name, Maff.centroid(placements.values), true, style.color)
      DrawMap.circle(goal, 32 * minTilesToGoal - 16, style.color)
      DrawMap.circle(goal, 32 * maxTilesToGoal - 16, style.color)
    }
  }
}

