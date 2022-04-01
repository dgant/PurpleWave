package Micro.Formation

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Information.Geography.Pathfinding.PathfindProfile
import Information.Geography.Pathfinding.Types.TilePath
import Information.Geography.Types.{Edge, Zone}
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{Pixel, Tile}
import Micro.Coordination.Pushing.TrafficPriorities
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Tactics.Squads.FriendlyUnitGroup
import Utilities.LightYear
import Utilities.Time.Minutes

class FormationStandard(val group: FriendlyUnitGroup, var style: FormationStyle, val goal: Pixel, var argZone: Option[Zone] = None) extends Formation {
  private case class ClassSlots(unitClass: UnitClass, var slots: Int, formationRangePixels: Double)
  private def units = group.groupFriendlyOrderable
  private def airUnits = units.filter(_.flying)
  private def groundUnits = units.filterNot(_.flying)
  private val paceAge = 48

  val pace11            : Double                = Maff.mean(groundUnits.view.map(u => Maff.clamp(Maff.nanToZero(u.pixelDistanceCenter(u.previousPixel(paceAge)) / u.topSpeed / paceAge), -1, 1)))
  val raceRangeTiles    : Int                   = if (With.frame > Minutes(4)() && With.enemies.exists(_.isProtoss)) 6 else if (With.enemies.exists(_.isTerran)) 4 else 1
  val expectRangeTiles  : Int                   = Math.max(raceRangeTiles, Maff.max(With.units.enemy.filter(u => u.unitClass.attacksGround && ! u.unitClass.isBuilding).view.map(_.formationRangePixels.toInt / 32)).getOrElse(0))
  val targetsNear       : Seq[UnitInfo]         = group.groupUnits.flatMap(_.battle).distinct.flatMap(_.enemy.units).filter(e => e.canAttack && group.groupUnits.exists(_.pixelsToGetInRangeTraveling(e) < 32 * 5))
  val targetsTowards    : Seq[UnitInfo]         = targetsNear.filter(_.pixelDistanceTravelling(goal) < group.centroidGround.groundPixels(goal))
  val targetNear        : Option[UnitInfo]      = Maff.minBy(targetsNear)(_.pixelDistanceTravelling(group.centroidGround))
  val targetTowards     : Option[UnitInfo]      = Maff.minBy(targetsTowards)(_.pixelDistanceTravelling(group.centroidGround))
  val target            : Pixel                 = targetTowards.orElse(targetNear).map(_.pixel).getOrElse(goal)
  val vanguardTarget    : Pixel                 = targetTowards.orElse(targetNear).map(_.pixel).getOrElse(With.scouting.threatOrigin.center)
  val vanguardOrigin    : Pixel                 = if (style == FormationStyleEngage || style == FormationStyleDisengage) vanguardTarget else goal
  val vanguardUnits     : Seq[FriendlyUnitInfo] = Maff.takePercentile(0.5, groundUnits)(Ordering.by(_.pixelDistanceTravelling(vanguardOrigin)))
  val centroid          : Pixel                 = Maff.weightedExemplar(vanguardUnits.view.map(u => (u.pixel, u.subjectiveValue))).walkablePixel
  val goalPath          : TilePath              =                                   new PathfindProfile(centroid.walkableTile, Some(goal.walkableTile),   lengthMaximum = Some(20), employGroundDist = true, costImmobility = 1.5).find
  val targetPath        : TilePath              = if (goal == target) goalPath else new PathfindProfile(centroid.walkableTile, Some(target.walkableTile), lengthMaximum = Some(0), employGroundDist = true, costImmobility = 1.5).find
  val goalPathTiles     : Seq[Tile]             = goalPath.tiles.getOrElse(Seq.empty).view
  val targetPathTiles   : Seq[Tile]             = targetPath.tiles.getOrElse(Seq.empty).view
  val goalPath5         : Tile                  = goalPathTiles.zipWithIndex.reverseIterator.find(p => p._2 <= 5).map(_._1).getOrElse(goal.walkableTile)
  val targetPath5       : Tile                  = targetPathTiles.zipWithIndex.reverseIterator.find(p => p._2 <= 5).map(_._1).getOrElse(target.walkableTile)
  val goalTowardsTarget : Boolean               = targetPath.pathExists && goalPath.pathExists && goalPath5.tileDistanceFast(targetPath5) < 6
  if (style == FormationStyleMarch && goalTowardsTarget && targetTowards.exists(t => units.exists(u => u.pixelsToGetInRange(t) < 32 * 6 && u.targetsAssigned.forall(_.contains(t))))) {
    style = FormationStyleEngage
  }
  val groupWidthPixels  : Int                   = groundUnits.map(_.unitClass.dimensionMax).sum
  val groupWidthTiles   : Int                   = Math.max(1, (16 + groupWidthPixels) / 32)
  val firstEdgeIndex    : Option[Int]           = goalPathTiles.indices.find(i => goalPathTiles(i).zone.edges.exists(_.contains(goalPathTiles(i))))
  val firstEdge         : Option[Edge]          = firstEdgeIndex.flatMap(i => goalPathTiles(i).zone.edges.find(_.contains(goalPathTiles(i))))
  val zone              : Zone                  = argZone.getOrElse(centroid.zone)
  val edge              : Option[Edge]          = Maff.minBy(zone.edges)(_.pixelCenter.groundPixels(goal))
  val altitudeInside    : Int                   = zone.centroid.altitude
  val altitudeOutside   : Int                   = edge.map(_.otherSideof(zone).centroid.altitude).getOrElse(altitudeInside)
  val altitudeRequired  : Int                   = if (style == FormationStyleGuard && expectRangeTiles > 1 && altitudeInside > altitudeOutside) altitudeInside else -1
  val maxThreat         : Int                   = if (style == FormationStyleEngage) With.grids.enemyRangeGround.margin - 1 else With.grids.enemyRangeGround.defaultValue
  val face              : Pixel                 = if (style == FormationStyleGuard) goal else goalPath5.center
  var apex              : Pixel                 = goal
  var stepSizePace      : Int                   = 0
  var stepSizeEngage    : Int                   = 0
  var stepSizeAssemble  : Int                   = 0
  var stepSizeEvade     : Int                   = 0
  var stepSizeCross     : Int                   = 0
  var stepSizeTiles     : Int                   = 0
  var minAltitude       : Int                   = -1

  val slots       : Map[UnitClass,        Seq[Pixel]] = slotsByClass()
  val placements  : Map[FriendlyUnitInfo, Pixel]      = UnassignedFormation(style, slots, group).outwardFromCentroid

  private def parameterize(): Unit = {
    if (style == FormationStyleGuard) {
      apex = edge.map(e => e.pixelCenter.project(e.endPixels.minBy(_.groundPixels(zone.centroid)), 32 * expectRangeTiles)).getOrElse(apex)
    } else if (goalPath.pathExists) {
      // Engage: Walk far enough to be in range
      // March: Walk far enough to advance our army
      // Disengage: Walk far enough to be comfortable outside enemy range
      // Move all the way past any narrow choke except one that's in our goal
      stepSizePace    = Maff.clamp(group.meanTopSpeed + 6 * (1 - pace11), 1, 12).toInt
      stepSizeCross   = firstEdgeIndex.flatMap(i => goalPathTiles.indices.drop(i).find(j => ! firstEdge.exists(_.contains(goalPathTiles(j))))).getOrElse(0)
      stepSizeEngage  = Maff.min(goalPathTiles.indices.filter(goalPathTiles(_).enemyVulnerabilityGround >= With.grids.enemyVulnerabilityGround.margin)).getOrElse(LightYear())
      stepSizeEvade   = 1 + groupWidthTiles + centroid.tile.enemyRangeGround
      stepSizeTiles   = Math.max(stepSizePace, stepSizeCross)
      if (style == FormationStyleDisengage) {
        stepSizeTiles = Math.max(stepSizeTiles, stepSizeEvade) // Make sure we go far enough to evade
      } else if (goalTowardsTarget) {
        stepSizeTiles = Math.min(stepSizeTiles, Math.max(1, stepSizeEngage)) // Don't try to move past the enemy
      }
      val maxTilesToGoal = centroid.tile.groundTiles(goal) - 1
      val minTilesToGoal = maxTilesToGoal - stepSizeTiles
      def scoreApex(tile: Tile): Double = {
        val distance = tile.groundTiles(goal)
        var output = distance - minTilesToGoal
        if (distance > maxTilesToGoal) output *= 10
        if (output < 0) output *= -100
        if (tile.zone.edges.exists(e => e.radiusPixels < groupWidthPixels / 4 && e.contains(tile.center))) output *= 10000
        output
      }
      apex = Maff.minBy(goalPathTiles)(scoreApex).map(_.center).getOrElse(goal)
    }
  }

  private def slotsByClass(): Map[UnitClass, Seq[Pixel]] = {
    parameterize()
    With.grids.formationSlots.reset()
    With.geography.ourBases.foreach(_.resourcePathTiles.foreach(With.grids.formationSlots.block))
    With.groundskeeper.reserved.view.map(_.target).foreach(With.grids.formationSlots.block)
    With.coordinator.pushes.all.view.filter(_.priority >= TrafficPriorities.Shove).foreach(_.tiles.foreach(With.grids.formationSlots.block))
    val classCount = groundUnits
      .groupBy(_.unitClass)
      .map(g => (ClassSlots(g._1, g._2.size, g._2.head.formationRangePixels / 32)))
      .toVector
      .sortBy(_.formationRangePixels)
    val arc = arcSlots(classCount, 32 + apex.pixelDistance(face))
    val groundPlacementCentroid = Maff.exemplarOption(arc.values.view.flatten).getOrElse(apex)
    val output = arc ++ airUnits
      .groupBy(_.unitClass)
      .map(u => (u._1, u._2.map(x => groundPlacementCentroid)))
    output
  }

  /**
    * Positions formation slots by tracing an arc around the goal, centered at the apex.
    */
  private def arcSlots(classSlots: Vector[ClassSlots], radius: Double): Map[UnitClass, Seq[Pixel]] = {
    var unitClass = Terran.Marine
    var angleIncrement = 0d
    val angleCenter = face.radiansTo(apex)
    val radiusIncrement = 4d
    var radius = face.pixelDistance(apex) - radiusIncrement
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
      true
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

  override def renderMap(): Unit = {
    super.renderMap()
    goalPath.renderMap(Colors.brighten(style.color))
    targetPath.renderMap(Colors.darken(style.color))
    DrawMap.polygon(Maff.convexHull(vanguardUnits.view.flatMap(_.corners)), Colors.MediumGray)
    if (goal == target) {
      DrawMap.label("Goal & Target", goal, drawBackground = true, style.color)
    } else {
      DrawMap.label("Target", target, drawBackground = true, style.color)
      DrawMap.label("Goal", goal, drawBackground = true, style.color)
    }
    DrawMap.label("Apex", apex, drawBackground = true, style.color)
    DrawMap.label("Centroid", centroid, drawBackground = true, style.color)
    if (placements.nonEmpty) {
      DrawMap.label(style.name, Maff.centroid(placements.values), drawBackground = true, style.color)
    }
  }
}
