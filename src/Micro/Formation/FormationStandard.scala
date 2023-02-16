package Micro.Formation

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Information.Geography.Pathfinding.Types.TilePath
import Information.Geography.Pathfinding.{PathfindProfile, PathfindRepulsor}
import Information.Geography.Types.{Edge, Zone}
import Lifecycle.With
import Mathematics.Points.{Pixel, Point, Points, Tile}
import Mathematics.{Maff, Shapes}
import Micro.Coordination.Pushing.TrafficPriorities
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Tactic.Squads.FriendlyUnitGroup
import Utilities.LightYear
import Utilities.Time.Minutes

//noinspection ComparingUnrelatedTypes
//Disabling spurious IntelliJ warnings
class FormationStandard(val group: FriendlyUnitGroup, var style: FormationStyle, val goal: Pixel, var argZone: Option[Zone] = None) extends Formation {
  private case class ClassSlots(unitClass: UnitClass, var slots: Int, formationRangePixels: Double)
  private def units       = group.unintended
  private def airUnits    = units.filter(_.flying)
  private def groundUnits = units.filterNot(_.flying)

  // + Glossary +
  // goal:             Where the group is trying to move. Travel formations (March/Disengage) want to pull the group towards this.
  // target:           Enemies we are likely to fight en route to our goal
  // apex:             The starting point from which we build the formation
  // face:             The point the formation should be facing
  val knownRangeTiles     : Int                   = Maff.max(With.units.enemy.filter(u => u.unitClass.attacksGround && ! u.unitClass.isBuilding).view.map(_.formationRangePixels.toInt / 32)).getOrElse(0)
  val raceRangeTiles      : Int                   = if (With.frame > Minutes(4)() && With.enemies.exists(_.isProtoss)) 6 else if (With.enemies.exists(_.isTerran)) 4 else 1
  val expectRangeTiles    : Int                   = Math.max(knownRangeTiles, raceRangeTiles)
  val targetsNear         : Seq[UnitInfo]         = group.groupUnits.flatMap(_.battle).distinct.flatMap(_.enemy.units).filter(e => group.groupUnits.exists(u => u.canAttack(e) && u.pixelsToGetInRange(e) < 32 * 5))
  val targetsTowards      : Seq[UnitInfo]         = targetsNear.filter(_.pixelDistanceTravelling(goal) < group.centroidGround.groundPixels(goal))
  val targetNear          : Option[UnitInfo]      = Maff.minBy(targetsNear)(_.pixelDistanceTravelling(group.centroidGround))
  val targetTowards       : Option[UnitInfo]      = Maff.minBy(targetsTowards)(_.pixelDistanceTravelling(group.centroidGround))
  val target              : Pixel                 = targetTowards.orElse(targetNear).map(_.pixel).getOrElse(goal)
  val threatOrigin        : Pixel                 = Maff.minBy(group.consensusPrimaryFoes.attackers.map(_.pixel))(_.pixelDistanceSquared(group.centroidKey)).getOrElse(With.scouting.enemyMuscleOrigin.center)
  val vanguardOrigin      : Pixel                 = if (style == FormationStyleEngage || style == FormationStyleDisengage) threatOrigin else goal
  val vanguardUnits       : Seq[FriendlyUnitInfo] = Maff.takePercentile(0.5, groundUnits)(Ordering.by(_.pixelDistanceTravelling(vanguardOrigin)))
  val vanguardCentroid    : Pixel                 = Maff.weightedExemplar(vanguardUnits.view.map(u => (u.pixel, u.subjectiveValue))).walkablePixel
  val goalPath            : TilePath              = findGoalPath
  val targetPath          : TilePath              = findTargetPath
  val goalPathTiles       : Seq[Tile]             = goalPath.tiles.getOrElse(Seq.empty).view
  val targetPathTiles     : Seq[Tile]             = targetPath.tiles.getOrElse(Seq.empty).view
  val goalPath5           : Tile                  = goalPathTiles.zipWithIndex.reverseIterator.find(p => p._2 <= 5).map(_._1).getOrElse(goal.walkableTile)
  val targetPath5         : Tile                  = targetPathTiles.zipWithIndex.reverseIterator.find(p => p._2 <= 5).map(_._1).getOrElse(threatOrigin.walkableTile)
  val goalTowardsTarget   : Boolean               = targetPath.pathExists && goalPath.pathExists && goalPath5.tileDistanceFast(targetPath5) < 6
  if (style == FormationStyleMarch && ! flanking && goalTowardsTarget && targetTowards.exists(t => units.exists(u => u.pixelsToGetInRange(t) < 32 * 6 && u.targetsAssigned.forall(_.contains(t))))) {
    style = FormationStyleEngage
  }
  val groupWidthPixels    : Int                   = groundUnits.map(_.unitClass.dimensionMax).sum
  val groupWidthTiles     : Int                   = Math.max(1, (16 + groupWidthPixels) / 32)
  val firstEdgeIndex      : Option[Int]           = goalPathTiles.indices.find(i => goalPathTiles(i).zone.edges.exists(_.contains(goalPathTiles(i))))
  val firstEdge           : Option[Edge]          = firstEdgeIndex.flatMap(i => goalPathTiles(i).zone.edges.find(_.contains(goalPathTiles(i))))
  val zone                : Zone                  = argZone.getOrElse(vanguardCentroid.zone)
  val edge                : Option[Edge]          = Maff.minBy(zone.edges)(_.pixelCenter.groundPixels(goal))
  val altitudeInside      : Int                   = zone.centroid.altitude
  val altitudeOutside     : Int                   = edge.map(_.otherSideof(zone).centroid.altitude).getOrElse(altitudeInside)
  val altitudeRequired    : Int                   = if (style == FormationStyleGuard && expectRangeTiles > 1 && altitudeInside > altitudeOutside) altitudeInside else -1
  val maxThreat           : Int                   = if (style == FormationStyleEngage) With.grids.enemyRangeGround.margin - 1 else With.grids.enemyRangeGround.defaultValue
  var face                : Pixel                 = if (style == FormationStyleEngage) threatOrigin else if (style == FormationStyleGuard) goal else goalPath5.center
  var apex                : Pixel                 = goal
  var stepTilesPace       : Int                   = 0
  var stepTilesEngage     : Int                   = 0
  var stepTilesAssemble   : Int                   = 0
  var stepTilesEvade      : Int                   = 0
  var stepTilesCross      : Int                   = 0
  var stepTiles           : Int                   = 0
  var minAltitude         : Int                   = -1

  private var _flanking: Boolean = false
  override def flanking: Boolean = _flanking

  val slots       : Map[UnitClass, Seq[Pixel]]    = slotsByClass()
  val unassigned  : UnassignedFormation           = UnassignedFormation(style, slots, group)
  val placements  : Map[FriendlyUnitInfo, Pixel]  = unassigned.outwardFromCentroid

  private def findGoalPath: TilePath = {
    val pathStart   = vanguardCentroid.zone
    val pathEnd     = goal.zone
    val pathToGoal  = With.paths.zonePath(pathStart, pathEnd)
    var pathToMarch = pathToGoal
    val choke       = pathToGoal.flatMap(_.steps.find(_.to == pathEnd))
    // Prefer flanking the enemy to attacking them through a nasty choke
    if (choke.exists(c => c.to == threatOrigin.zone && c.edge.badness(group, vanguardCentroid.zone) > 1.5)) {
      val paths = pathEnd.edges.flatMap(e => With.paths.zonePath(pathStart, pathEnd, Seq(e))).filter(_.steps.nonEmpty)
      val best = Maff.minBy(paths)(p => p.length * p.steps.last.edge.badness(group, p.steps.last.from))
      if (best.exists(b => pathToMarch.exists(_.length < b.length))) {
        _flanking = true
      }
      pathToMarch = best
    }
    val waypoint = pathToMarch.flatMap(_.steps.headOption.map(_.to.centroid)).getOrElse(goal.walkableTile)
    new PathfindProfile(
      vanguardCentroid.walkableTile,
      Some(waypoint),
      lengthMaximum = Some(20),
      employGroundDist = true,
      costImmobility = 1.5,
      repulsors = Vector(PathfindRepulsor(Points.middle, -0.1, With.mapPixelHeight))).find
  }
  private def findTargetPath: TilePath = {
    if (goal == threatOrigin) return goalPath
    new PathfindProfile(vanguardCentroid.walkableTile, Some(threatOrigin.walkableTile), lengthMaximum = Some(0),  employGroundDist = true, costImmobility = 1.5, repulsors = Vector(PathfindRepulsor(Points.middle, -0.1, With.mapPixelHeight))).find
  }

  private def parameterize(): Unit = {
    if (style == FormationStyleGuard) {
      val guardDepth  = 32 * expectRangeTiles
      val guardRadius = guardDepth + edge.map(_.radiusPixels).getOrElse(32d)
      apex = edge.map(e => e.pixelCenter.project(e.endPixels.minBy(_.groundPixels(zone.centroid)), guardDepth)).getOrElse(apex)
      face = apex.project(face, guardRadius) // Extend the face outwards to broaden our arc
    } else if (goalPath.pathExists) {
      // Engage: Walk far enough to be in range
      // March: Walk far enough to advance our army
      // Disengage: Walk far enough to be comfortable outside enemy range
      // Move all the way past any narrow choke except one that's in our goal
      stepTilesPace   = Maff.clamp(group.meanTopSpeed * 24 / 32 + 6 * Math.max(0.0, 1 - 2.0 * group.pace01), 1, 8).toInt
      stepTilesCross  = firstEdgeIndex.flatMap(i => goalPathTiles.indices.drop(i).find(j => ! firstEdge.exists(_.contains(goalPathTiles(j))))).getOrElse(0)
      stepTilesEngage = Maff.min(goalPathTiles.indices.filter(goalPathTiles(_).enemyVulnerabilityGround >= With.grids.enemyVulnerabilityGround.margin)).getOrElse(LightYear())
      stepTilesEvade  = 1 + groupWidthTiles + vanguardCentroid.tile.enemyRangeGround
      stepTiles       = Math.max(stepTilesPace, stepTilesCross)
      if (style == FormationStyleDisengage) {
        stepTiles = Math.max(stepTiles, stepTilesEvade) // Make sure we go far enough to evade
      } else if (goalTowardsTarget) {
        stepTiles = Math.min(stepTiles, Math.max(1, stepTilesEngage)) // Don't try to move past the enemy
      }
      val maxTilesToGoal = vanguardCentroid.tile.groundTiles(goal) - 1
      val minTilesToGoal = maxTilesToGoal - stepTiles
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
    With.groundskeeper.reserved.foreach(With.grids.formationSlots.block)
    With.coordinator.pushes.all.view.filter(_.priority >= TrafficPriorities.Shove).foreach(_.tiles.foreach(With.grids.formationSlots.block))
    if (style == FormationStyleGuard && groundUnits.exists(Protoss.Reaver)) {
      Shapes.Ray(face, vanguardCentroid).foreach(With.grids.formationSlots.block) // Clear path for Scarabs
    }
    val classCount = groundUnits
      .groupBy(_.unitClass)
      .map(g => (ClassSlots(g._1, g._2.size, g._2.head.formationRangePixels / 32)))
      .toVector
      .sortBy(_.formationRangePixels)

    val classSlotsOverlook: Map[UnitClass, Seq[Pixel]] = Map.empty /*
      if      (style == FormationStyleGuard)  overlookSlots(classCount, requireTarget = false)
      else if (style == FormationStyleEngage) overlookSlots(classCount, requireTarget = true)
      else                                    Map.empty */
    val classCountRemaining = classCount.map(c => c.slots - classSlotsOverlook.get(c.unitClass).map(_.length).getOrElse(0))
    val classSlotsArc       = arcSlots(classCount, 32 + apex.pixelDistance(face))
    val groundExemplar      = Maff.exemplarOpt(classSlotsArc.values.view.flatten).getOrElse(apex)
    val classSlotsAir       = airUnits.groupBy(_.unitClass).map(u => (u._1, u._2.map(x => groundExemplar)))
    val output              = classSlotsOverlook ++ classSlotsArc ++ classSlotsAir
    output
  }

  /**
    * Positions formation slots by using overlooks from a nearby base
    */
  private def overlookSlots(classSlots: Vector[ClassSlots], requireTarget: Boolean = false): Map[UnitClass, Seq[Pixel]] = {
    val overlooks = goal.base.filter(base => argZone.forall(_.bases.contains(base))).map(_.overlooks).getOrElse(Vector.empty)
    var i = 0
    val output = classSlots
      .filter(slots => ! slots.unitClass.isFlyer && slots.unitClass != Protoss.Reaver && slots.unitClass != Terran.Medic && slots.unitClass.effectiveRangePixels >= 128)
      .map(slots => (slots.unitClass, (0 until slots.slots)
        .map(unused => {
          var output: Pixel = null
          while (output == null && i < overlooks.length) {
            val overlook = overlooks(i)
            lazy val rangeAppropriate = overlook._2 <= slots.formationRangePixels + 96
            lazy val enemyInRange     = overlook._1.enemyVulnerabilityGround <= With.grids.enemyVulnerabilityGround.margin
            if (enemyInRange || ( ! requireTarget && rangeAppropriate)) {
              output = overlook._1.center
            }
            i += 1
          }
          output
        }).filter(_ != null)))
      .toMap
    output.foreach(o => o._2.foreach(p => With.grids.formationSlots.tryPlace(o._1, p)))
    output
  }

  /**
    * Positions formation slots by tracing an arc around the goal, centered at the apex.
    */
  private def arcSlots(classSlots: Vector[ClassSlots], radius: Double): Map[UnitClass, Seq[Pixel]] = {
    var unitClass       = Terran.Marine
    var angleIncrement  = 0d
    val angleCenter     = face.radiansToSlow(apex)
    val radiusIncrement = 4d
    var radius          = face.pixelDistance(apex) - radiusIncrement
    var rowSlot         = -1
    var slotsEvaluated  = 0
    var haltNegative    = false
    var haltPositive    = false
    var arcZone: Zone   = null
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
    goalPath.renderMap(Colors.brighten(style.color), customOffset = Point(-3, -3))
    targetPath.renderMap(Colors.darken(style.color), customOffset = Point(3, 3))
    DrawMap.polygon(Maff.convexHull(vanguardUnits.view.flatMap(_.corners)), Colors.DarkGray)
    Seq(("Goal", goal), ("Target", threatOrigin), ("Face", face), ("Apex", apex), ("Centroid", vanguardCentroid))
      .groupBy(_._2)
      .foreach(p => DrawMap.label(p._2.map(_._1).mkString(" & "), p._1, drawBackground = true, style.color))
    if (placements.nonEmpty) {
      DrawMap.label(style.name, Maff.centroid(placements.values), drawBackground = true, style.color)
    }
  }
}
