package Micro.Formation

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Information.Geography.Pathfinding.PathfindProfile
import Information.Geography.Pathfinding.Types.TilePath
import Information.Geography.Types.{Edge, Zone}
import Information.Grids.Floody.GridEnemyVulnerabilityGround
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{Pixel, Tile}
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Tactics.Squads.FriendlyUnitGroup
import Utilities.LightYear
import Utilities.Time.Minutes

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class FormationStandard(val group: FriendlyUnitGroup, var style: FormationStyle, val goal: Pixel, var argZone: Option[Zone] = None) extends Formation {
  private case class ClassSlots(unitClass: UnitClass, var slots: Int, formationRangePixels: Double)
  private def units = group.groupFriendlyOrderable
  private def airUnits = units.filter(_.flying)
  private def groundUnits = units.filterNot(_.flying)

  val raceRangeTiles    : Int                   = if (With.frame > Minutes(4)() && With.enemies.exists(_.isProtoss)) 6 else if (With.enemies.exists(_.isTerran)) 4 else 1
  val expectRangeTiles  : Int                   = Math.max(raceRangeTiles, Maff.max(With.units.enemy.filter(u => u.unitClass.attacksGround && ! u.unitClass.isBuilding).view.map(_.formationRangePixels.toInt / 32)).getOrElse(0))
  val targetsAll        : Seq[UnitInfo]         = group.groupUnits.flatMap(_.battle).distinct.flatMap(_.enemy.units).filter(e => group.groupUnits.exists(_.canAttack(e)))
  val targetsTowards    : Seq[UnitInfo]         = targetsAll.filter(_.pixelDistanceTravelling(goal) < group.centroidGround.groundPixels(goal))
  val targetAll         : Option[UnitInfo]      = Maff.minBy(targetsAll)(_.pixelDistanceTravelling(group.centroidGround))
  val targetTowards     : Option[UnitInfo]      = Maff.minBy(targetsTowards)(_.pixelDistanceTravelling(group.centroidGround))
  val target            : Pixel                 = targetTowards.orElse(targetAll).map(_.pixel).getOrElse(With.scouting.threatOrigin.center)
  val vanguardOrigin    : Pixel                 = if (style == FormationStyleEngage || style == FormationStyleDisengage) target else goal
  val vanguardUnits     : Seq[FriendlyUnitInfo] = Maff.takePercentile(0.5, groundUnits)(Ordering.by(_.pixelDistanceTravelling(vanguardOrigin)))
  val centroid          : Pixel                 = Maff.weightedExemplar(vanguardUnits.view.map(u => (u.pixel, u.subjectiveValue))).walkablePixel
  val goalPath          : TilePath              = new PathfindProfile(centroid.walkableTile, Some(goal.walkableTile),   lengthMaximum = Some(20), employGroundDist = true, costImmobility = 1.5).find
  val targetPath        : TilePath              = new PathfindProfile(centroid.walkableTile, Some(target.walkableTile), lengthMaximum = Some(10), employGroundDist = true, costImmobility = 1.5).find
  val goalPathTiles     : Seq[Tile]             = goalPath.tiles.getOrElse(Seq.empty).view
  val targetPathTiles   : Seq[Tile]             = targetPath.tiles.getOrElse(Seq.empty).view
  val goalPath5         : Tile                  = goalPathTiles.zipWithIndex.reverseIterator.find(p => p._2 <= 5).map(_._1).getOrElse(centroid.walkableTile)
  val targetPath5       : Tile                  = targetPathTiles.zipWithIndex.reverseIterator.find(p => p._2 <= 5).map(_._1).getOrElse(centroid.walkableTile)
  val goalTowardsTarget : Boolean               = targetPath.pathExists && goalPath.pathExists && goalPath5.tileDistanceFast(targetPath5) < 6
  val groupWidthPixels  : Int                   = groundUnits.map(_.unitClass.dimensionMax).sum
  val groupWidthTiles   : Int                   = Math.max(1, (16 + groupWidthPixels) / 32)
  val firstEdgeIndex    : Option[Int]           = goalPathTiles.indices.find(i => goalPathTiles(i).zone.edges.exists(_.contains(goalPathTiles(i))))
  val firstEdge         : Option[Edge]          = firstEdgeIndex.flatMap(i => goalPathTiles(i).zone.edges.find(_.contains(goalPathTiles(i))))
  val zone              : Zone                  = argZone.getOrElse(centroid.zone)
  val edge              : Option[Edge]          = Maff.minBy(zone.edges)(_.pixelCenter.groundPixels(goal))
  val altitudeInside    : Int                   = zone.centroid.altitude
  val altitudeOutside   : Int                   = edge.map(_.otherSideof(zone).centroid.altitude).getOrElse(altitudeInside)
  var altitudeRequired  : Int                   = if (expectRangeTiles > 1 && altitudeInside > altitudeOutside) altitudeInside else -1
  val face              : Pixel                 =
    if (style == FormationStyleEngage || style == FormationStyleMarch) targetTowards.map(_.pixel).getOrElse(goal)
    else if (style == FormationStyleGuard) goal
    else targetTowards
      .orElse(targetAll)
      .map(_.pixel)
      .orElse(targetPath.tiles.map(p => p.find( ! _.visible).getOrElse(p.last).center))
      .getOrElse(target)
  var apex              : Pixel                 = goal
  var stepSizePace      : Int                   = 0
  var stepSizeEngage    : Int                   = 0
  var stepSizeAssemble  : Int                   = 0
  var stepSizeEvade     : Int                   = 0
  var stepSizeCross     : Int                   = 0
  var stepSizeTiles     : Int                   = 0
  var minTilesToGoal    : Int                   = - LightYear()
  var maxTilesToGoal    : Int                   = LightYear()
  var maxThreat         : Int                   = LightYear()
  var minAltitude       : Int                   = -1
  var costDistanceGoal  : Int                   = 1
  var costDistanceApex  : Int                   = 1
  var costThreat        : Int                   = 0
  var costVulnerability : Int                   = 0

  parameterize()

  val slots: Map[UnitClass, Seq[Pixel]] = slotsByClass()
  val placements: Map[FriendlyUnitInfo, Pixel] = UnassignedFormation(style, slots, group).outwardFromCentroid

  private def parameterize(): Unit = {
    if (style == FormationStyleMarch && goalTowardsTarget && targetTowards.exists(t => units.exists(u => u.pixelsToGetInRange(t) < 32 * 6 && u.targetsAssigned.forall(_.contains(t))))) {
      style = FormationStyleEngage
    }
    if (style == FormationStyleGuard) {
      apex              = edge.map(_.endPixels.minBy(_.groundPixels(zone.centroid))).getOrElse(apex)
      minTilesToGoal    = expectRangeTiles
      costDistanceGoal  = 5
      if (altitudeInside <= altitudeOutside) {
        maxThreat = vGrid.margin
      }
    } else {
      altitudeRequired = -1
    }
    if (style == FormationStyleEngage) {
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
        stepSizeEngage  = Maff.min(goalPathTiles.indices.filter(goalPathTiles(_).enemyVulnerabilityGround >= vGrid.margin)).getOrElse(LightYear())
        stepSizeEvade   = 1 + groupWidthTiles + centroid.tile.enemyRangeGround
        stepSizeTiles   = Math.max(stepSizePace, stepSizeCross)
        if (style == FormationStyleDisengage) {
          stepSizeTiles = Math.max(stepSizeTiles, stepSizeEvade) // Make sure we go far enough to evade
        } else if (goalTowardsTarget) {
          stepSizeTiles = Math.min(stepSizeTiles, Math.max(1, stepSizeEngage)) // Don't try to move past the enemy
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
    With.grids.formationSlots.reset()
    With.geography.ourBases.foreach(_.resourcePathTiles.foreach(t => With.grids.formationSlots.block(t.center)))
    With.groundskeeper.reserved.view.map(_.target).foreach(t => With.grids.formationSlots.block(t.center))

    lazy val ourArcSlots = arcSlots(classCount, 32 + apex.pixelDistance(face))
    lazy val ourFloodSlots = floodSlots(classCount)
    var output = ourArcSlots
    if (output.size < groundUnits.size) {
      output = if (ourArcSlots.size >= ourFloodSlots.size) ourArcSlots else ourFloodSlots
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
          && (maxThreat       >= tile.enemyRangeGroundUnchecked)
          && (minAltitude     <= tile.altitudeUnchecked)
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
    var unitClass = Terran.Marine
    var angleIncrement = 0d
    val angleCenter = face.radiansTo(apex)
    val radiusIncrement = 4d
    var radius = Math.max(0, face.pixelDistance(apex) - radiusIncrement)
    var rowSlot = -1
    var slotsEvaluated = 0
    var haltNegative = false
    var haltPositive = false
    var arcZone: Zone = null
    def nextRow(): Boolean = {
      radius += radiusIncrement
      angleIncrement = radiusIncrement / radius
      rowSlot = -1
      haltNegative = false
      haltPositive = false
      return true
    }
    nextRow()
    def proceed(): Boolean = {
      slotsEvaluated += 1
      if (slotsEvaluated > 10000) return false
      rowSlot += 1
      val s = if (rowSlot % 2 == 0) 1 else -1
      val m = s * rowSlot / 2
      val angleDelta = angleIncrement * m
      if (angleDelta > Maff.halfPI) return nextRow()
      if (s < 0 && haltNegative) return true
      if (s > 0 && haltPositive) return true
      val p = face.radiateRadians(angleCenter + angleDelta, radius)
      if (rowSlot == 0) { arcZone = p.zone }
      if ( ! p.walkable) {
        if ( ! p.walkableTerrain && p.zone != arcZone) {
          haltNegative ||= s < 0
          haltPositive ||= s > 0
          if (haltNegative && haltPositive) return nextRow()
        }
        return true
      }
      val groundTilesToGoal = p.groundPixels(goal) / 32
      if (groundTilesToGoal > maxTilesToGoal) return false // We've gone too far and will never find a slot
      //if (groundTilesToGoal < minTilesToGoal) return true // If we keep going we may find a slot
      if (p.altitude < minAltitude) return true
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
    DrawMap.label("Target", target.add(0, 16), drawBackground = true, style.color)
    DrawMap.label("Goal", goal, drawBackground = true, style.color)
    DrawMap.label("Apex", apex, drawBackground = true, style.color)
    DrawMap.label("Centroid", centroid, drawBackground = true, style.color)
    if (placements.nonEmpty) {
      DrawMap.label(style.name, Maff.centroid(placements.values), drawBackground = true, style.color)
    }
    DrawMap.circle(goal, 32 * minTilesToGoal - 16, style.color)
    DrawMap.circle(goal, 32 * maxTilesToGoal - 16, style.color)
  }
}
