package Micro.Formation

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Information.Geography.Pathfinding.PathfindProfile
import Information.Grids.Floody.GridEnemyVulnerabilityGround
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{Pixel, Tile}
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
  val centroid          = Maff.weightedExemplar(vanguardUnits.view.map(u => (u.pixel, u.subjectiveValue))).walkablePixel
  val goalPath          = new PathfindProfile(centroid.walkableTile, Some(goal.walkableTile),   lengthMaximum = Some(20), employGroundDist = true, costImmobility = 1.5).find
  val targetPath        = new PathfindProfile(centroid.walkableTile, Some(target.walkableTile), lengthMaximum = Some(10), employGroundDist = true, costImmobility = 1.5).find
  val goalPathTiles     = goalPath.tiles.getOrElse(Seq.empty).view
  lazy val goalPath5    = goalPath.tiles.get.zipWithIndex.reverseIterator.find(p => p._2 <= 5).get._1
  lazy val targetPath5  = targetPath.tiles.get.zipWithIndex.reverseIterator.find(p => p._2 <= 5).get._1
  val goalTowardsTarget = targetPath.pathExists && goalPath.pathExists && goalPath5.tileDistanceFast(targetPath5) < 6
  val groupWidthPixels  = groundUnits.map(_.unitClass.dimensionMax).sum
  val groupWidthTiles   = Math.max(1, (16 + groupWidthPixels) / 32)
  val firstEdgeIndex    = goalPathTiles.indices.find(i => goalPathTiles(i).zone.edges.exists(_.contains(goalPathTiles(i))))
  val firstEdge         = firstEdgeIndex.flatMap(i => goalPathTiles(i).zone.edges.find(_.contains(goalPathTiles(i))))
  val face              = if (style == FormationStyleEngage || style == FormationStyleMarch) {
    targetTowards.map(_.pixel).getOrElse(goal)
  } else {
    targetTowards.orElse(targetAll).map(_.pixel).orElse(targetPath.tiles.map(p => p.find( ! _.visible).getOrElse(p.last).center)).getOrElse(target)
  }
  var apex              = goal
  var stepSizePace      = 0
  var stepSizeEngage    = 0
  var stepSizeAssemble  = 0
  var stepSizeEvade     = 0
  var stepSizeCross     = 0
  var stepSizeTiles     = 0
  var minTilesToGoal    = - LightYear()
  var maxTilesToGoal    = LightYear()
  var maxThreat         = LightYear()
  var costDistanceGoal  = 1
  var costDistanceApex  = 1
  var costThreat        = 0
  var costVulnerability = 0
  parameterize()
  val slots: Map[UnitClass, Seq[Pixel]] = slotsByClass()
  val placements: Map[FriendlyUnitInfo, Pixel] = UnassignedFormation(style, slots, group).outwardFromCentroid

  private def parameterize(): Unit = {
    if (style == FormationStyleGuard) {
      minTilesToGoal    = 1
      costDistanceGoal  = 5
    } else if (style == FormationStyleEngage) {
      costVulnerability = 1
      costThreat        = 1
    }
    if (style == FormationStyleEngage || style == FormationStyleMarch || style == FormationStyleDisengage) {
      if (goalPath.pathExists) {
        // Engage: Walk far enough to be in range
        // March: Walk far enough to advance our army
        // Disengage: Walk far enough to be comfortable outside enemy range
        // Move all the way past any narrow choke except one that's in our goal
        stepSizePace    = Maff.clamp((16 + 24 * group.meanTopSpeed) / 32, 1, 8).toInt
        stepSizeCross   = firstEdgeIndex.flatMap(i => goalPathTiles.indices.drop(i).find(j => ! firstEdge.exists(_.contains(goalPathTiles(j))))).getOrElse(0)
        stepSizeEngage  = Maff.min(goalPathTiles.indices.filter(goalPathTiles(_).enemyVulnerabilityGround >= vGrid.margin)).getOrElse(0)
        stepSizeEvade   = 1 + groupWidthTiles + centroid.tile.enemyRangeGround
        stepSizeTiles   = Math.max(stepSizePace, stepSizeCross)
        if (style == FormationStyleDisengage) {
          stepSizeTiles = Math.max(stepSizeTiles, stepSizeEvade) // Make sure we go far enough to evade
        } else if (goalTowardsTarget) {
          if (style == FormationStyleEngage) {
            stepSizeTiles = stepSizeEngage // Make sure we go far enough to fight
          } else {
            stepSizeTiles = Math.min(stepSizeTiles, stepSizeEngage) // Don't try to move past the enemy
          }
        }
        maxTilesToGoal    = centroid.tile.groundTiles(goal) - 1
        minTilesToGoal    = maxTilesToGoal - stepSizeTiles
        maxThreat         = if (style == FormationStyleMarch) With.grids.enemyRangeGround.margin else With.grids.enemyRangeGround.defaultValue
        apex = goalPathTiles
          .reverseIterator
          .filterNot(t => t.zone.edges.exists(e => e.radiusPixels < groupWidthPixels / 4 && e.contains(t.center)))
          .find(_.groundTiles(goal) >= minTilesToGoal)
          .orElse(goalPathTiles.find(_.groundTiles(goal) == minTilesToGoal + 1))
          .map(_.center)
          .getOrElse(goal)
      }
    }
  }

  private def slotsByClass(): Map[UnitClass, Seq[Pixel]] = {
    val classCount = groundUnits
      .groupBy(_.unitClass)
      .map(g => (ClassSlots(g._1, g._2.size, g._2.head.formationRangePixels / 32)))
      .toVector
      .sortBy(_.formationRangePixels)
    val preferArc = true //style == FormationStyleGuard || style == FormationStyleEngage || PixelRay(centroid, apex).forall(_.walkable)
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
    DrawMap.polygon(Maff.convexHull(vanguardUnits.view.flatMap(_.corners)), Colors.MediumGray)
    DrawMap.label("Target", target.add(0, 16), true, style.color)
    DrawMap.label("Goal", goal, true, style.color)
    DrawMap.label("Apex", apex, true, style.color)
    DrawMap.label("Centroid", centroid, true, style.color)
    if (placements.nonEmpty) {
      DrawMap.label(style.name, Maff.centroid(placements.values), true, style.color)
    }
    DrawMap.circle(goal, 32 * minTilesToGoal - 16, style.color)
    DrawMap.circle(goal, 32 * maxTilesToGoal - 16, style.color)
  }
}
