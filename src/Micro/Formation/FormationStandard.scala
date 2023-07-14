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
import Utilities.Time.Minutes
import Utilities.{?, LightYear}

//noinspection ComparingUnrelatedTypes
//Disabling spurious IntelliJ warnings
final class FormationStandard(val group: FriendlyUnitGroup, var style: FormationStyle, val goal: Pixel, var argZone: Option[Zone] = None) extends Formation {
  private case class ClassSlots(unitClass: UnitClass, var slots: Int, formationRangePixels: Double)
  private def units       = group.unintended
  private def airUnits    = units.filter(_.flying)
  private def groundUnits = units.filterNot(_.flying)
  private def keyUnits    = Maff.orElse(groundUnits, airUnits)

  // + Glossary +
  // goal:             Where the group is trying to move. Travel formations (March/Disengage) want to pull the group towards this.
  // target:           Enemies we are likely to fight en route to our goal
  // apex:             The starting point from which we build the formation
  // face:             The point the formation should be facing
  val knownRangeTiles     : Int                   = Maff.div32(Maff.max(With.units.enemy.filter(u => u.unitClass.attacksGround && ! u.unitClass.isBuilding).view.map(_.formationRangePixels)).getOrElse(0.0).toInt)
  val raceRangeTiles      : Int                   = if (With.frame > Minutes(4)() && With.enemies.exists(_.isProtoss)) 6 else if (With.enemies.exists(_.isTerran)) 4 else 1
  val expectRangeTiles    : Int                   = Math.max(knownRangeTiles, raceRangeTiles)
  val targetsNear         : Seq[UnitInfo]         = group.groupUnits.flatMap(_.battle).distinct.flatMap(_.enemy.units).filter(e => group.groupUnits.exists(u => u.canAttack(e) && u.pixelsToGetInRange(e) < 32 * 5))
  val targetsTowards      : Seq[UnitInfo]         = targetsNear.filter(_.pixelDistanceTravelling(goal) < group.centroidGround.groundPixels(goal))
  val targetNear          : Option[UnitInfo]      = Maff.minBy(targetsNear)(_.pixelDistanceTravelling(group.centroidGround))
  val targetTowards       : Option[UnitInfo]      = Maff.minBy(targetsTowards)(_.pixelDistanceTravelling(group.centroidGround))
  val target              : Pixel                 = targetTowards.orElse(targetNear).map(_.pixel).getOrElse(goal)
  val threatOrigin        : Pixel                 = Maff.minBy(group.consensusPrimaryFoes.attackers.map(_.pixel))(_.pixelDistanceSquared(group.centroidKey)).getOrElse(With.scouting.enemyMuscleOrigin.center)
  val vanguardOrigin      : Pixel                 = if (style == FormationStyleEngage || style == FormationStyleDisengage) threatOrigin else goal
  val vanguardUnits       : Seq[FriendlyUnitInfo] = Maff.takePercentile(0.5, keyUnits)(Ordering.by(_.pixelDistanceTravelling(vanguardOrigin)))
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
  var face                : Pixel                 = if (style == FormationStyleEngage) threatOrigin else goal // This will usually be overridden
  var apex                : Pixel                 = goal // This will usually be overridden
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
    val zonePathStart   = vanguardCentroid.zone
    val zonePathEnd     = goal.zone
    val zonePathToGoal  = With.paths.zonePath(zonePathStart, zonePathEnd)
    var zonePathToMarch = zonePathToGoal
    val choke           = zonePathToGoal.flatMap(_.steps.find(_.to == zonePathEnd))

    // Prefer flanking the enemy over attacking them through a nasty choke
    if (choke.exists(c => c.to == threatOrigin.zone && c.edge.badness(group, vanguardCentroid.zone) > 1.5)) {
      val paths = zonePathEnd.edges.flatMap(e => With.paths.zonePath(zonePathStart, zonePathEnd, Seq(e))).filter(_.steps.nonEmpty)
      val best = Maff.minBy(paths)(p => p.length * p.steps.last.edge.badness(group, p.steps.last.from))
      if (best.exists(b => zonePathToMarch.exists(_.length < b.length))) {
        _flanking = true
      }
      zonePathToMarch = best
    }
    val waypoint = zonePathToMarch.flatMap(_.steps.headOption.map(_.to.centroid)).getOrElse(goal.walkableTile)
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
    new PathfindProfile(
      vanguardCentroid.walkableTile,
      Some(threatOrigin.walkableTile),
      lengthMaximum = Some(0),
      employGroundDist = true,
      costImmobility = 1.5,
      repulsors = Vector(PathfindRepulsor(Points.middle, -0.1, With.mapPixelHeight))).find
  }

  private def parameterize(): Unit = {
    if (style == FormationStyleGuard) {
      val guardDepth  = 32 * expectRangeTiles
      val guardRadius = guardDepth + edge.map(_.radiusPixels).getOrElse(32d)
      apex = edge.map(e => e.pixelCenter.project(e.endPixels.minBy(p => p.groundPixels(zone.centroid) + 0.01 * p.pixelDistance(zone.centroid.center)), guardDepth)).getOrElse(apex)
      face = apex.project(face, guardRadius) // Extend the face outwards to broaden our arc
    } else if (goalPath.pathExists) {
      // Engage:    Walk far enough to be in range
      // March:     Walk far enough to advance our army
      // Disengage: Walk far enough to be comfortable outside enemy range
      // Move all the way past any narrow choke except one that's in our goal
      stepTilesPace   = Maff.clamp(group.meanTopSpeed * 24 / 32 + 6 * Math.max(0.0, 1 - 2.0 * group.pace01), 1, 8).toInt
      stepTilesCross  = ?(firstEdge.exists(_.diameterPixels < groupWidthPixels * 2.5), firstEdgeIndex.flatMap(i => goalPathTiles.indices.drop(i).find(j => ! firstEdge.exists(_.contains(goalPathTiles(j))))).getOrElse(0), 0)
      stepTilesEngage = Maff.min(goalPathTiles.indices.filter(goalPathTiles(_).enemyVulnerabilityGround >= With.grids.enemyVulnerabilityGround.margin)).getOrElse(LightYear())
      stepTilesEvade  = 1 + groupWidthTiles + vanguardCentroid.tile.enemyRangeGround
      stepTiles       = Math.max(stepTilesPace, stepTilesCross)
      if (style == FormationStyleDisengage) {
        stepTiles = Math.max(stepTiles, stepTilesEvade) // Make sure we go far enough to evade
      } else if (goalTowardsTarget) {
        stepTiles = Math.min(stepTiles, Math.max(1, stepTilesEngage)) // Don't try to move past the enemy
      }
      def scoreApex(i: Int, tile: Tile): Double = {
        var output = stepTiles - i
        if (output < 0) output *= 1000
        output = Math.abs(output)
        if (tile.zone.edges.exists(e => e.radiusPixels < groupWidthPixels / 4 && e.contains(tile.center))) output *= 100
        output
      }
      val apexI = Maff.minBy(goalPathTiles.indices)(i => scoreApex(i, goalPathTiles(i)))
      apex      = apexI.map(goalPathTiles(_).center).getOrElse(goal)
      face      = targetNear
        .map(_.pixel)
        .orElse(apexI.map(_ + 1 + Maff.div32(group.meanAttackerRange.toInt)).flatMap(goalPathTiles.drop(_).lastOption).map(_.center))
        .getOrElse(goal)
    }
  }

  private def slotsByClass(): Map[UnitClass, Seq[Pixel]] = {
    parameterize()
    With.grids.formationSlots.reset()
    With.geography.ourBases.foreach(_.resourcePathTiles.foreach(With.grids.formationSlots.block))
    With.groundskeeper.reserved.foreach(With.grids.formationSlots.block)
    With.coordinator.pushes.all.view.filter(_.priority >= TrafficPriorities.Shove).foreach(_.tiles.foreach(With.grids.formationSlots.block))
    if (style == FormationStyleGuard && groundUnits.exists(Protoss.Reaver)) {
      Shapes.Ray(face, vanguardCentroid).map(_.tile).foreach(With.grids.formationSlots.block) // Clear path for Scarabs
    }
    val classCount = groundUnits
      .groupBy(_.unitClass)
      .map(g => (ClassSlots(g._1, g._2.size, g._2.head.formationRangePixels * Maff.inv32)))
      .toVector
      .sortBy(_.formationRangePixels)

    val classSlotsOverlook: Map[UnitClass, Seq[Pixel]] = Map.empty /*
      if      (style == FormationStyleGuard)  overlookSlots(classCount, requireTarget = false)
      else if (style == FormationStyleEngage) overlookSlots(classCount, requireTarget = true)
      else                                    Map.empty */
    var classSlotsArc         = arcSlots(face, classCount, 32 + apex.pixelDistance(face))
    lazy val arcSlotsDesired  = 0.8 * classCount.view.map(_.slots).sum
    lazy val arcSlotsProduced = classSlotsArc.view.map(_._2.length).sum
    if (style == FormationStyleGuard && face.tile.enemiesAttackingGround.nonEmpty && arcSlotsProduced < arcSlotsDesired) {
      // We have failed to produce an acceptable guard formatio , likely because the choke has been breached.
      // Let's back up one choke and try again
      val alternateFaces = goalPathTiles.takeWhile(_ != face.zone).lastOption
      alternateFaces.map(_.center).foreach(alternateFace => {
        val alternateClassSlotsArc    = arcSlots(alternateFace, classCount, 32 + apex.pixelDistance(alternateFace))
        val alternateArcSlotsProduced = alternateClassSlotsArc.view.map(_._2.length).sum
        if (alternateArcSlotsProduced > arcSlotsProduced) {
          With.logger.debug(f"Substituting Guard arc from $face to $alternateFace because that results in $alternateArcSlotsProduced/$arcSlotsDesired > $arcSlotsDesired/$arcSlotsDesired slots")
          face = alternateFace
          classSlotsArc = alternateClassSlotsArc
        }
      })
    }
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
  private def arcSlots(toFace: Pixel, classSlots: Vector[ClassSlots], radius: Double): Map[UnitClass, Seq[Pixel]] = {
    var unitClass       = Terran.Marine
    var angleIncrement  = 0d
    val angleCenter     = toFace.radiansToSlow(apex)
    val radiusIncrement = 4d
    var radius          = toFace.pixelDistance(apex) - radiusIncrement
    var rowSlot         = -1
    var slotsEvaluated  = 0
    var haltNegative    = false
    var haltPositive    = false
    var arcZone: Zone   = null
    def nextRow(): Boolean = {
      radius          += radiusIncrement
      angleIncrement  = radiusIncrement / radius
      rowSlot         = -1
      haltNegative    = false
      haltPositive    = false
      true
    }
    nextRow()

    // Why can't Scala just have break statements like a normal language
    var i = 0
    while (i < classSlots.length) {
      unitClass = classSlots(i).unitClass
      var j = 0
      while (j < classSlots(i).slots) {
        var proceed = true
        while (proceed) {
          slotsEvaluated += 1
          if (slotsEvaluated > 10000) {
            proceed = false
          } else {
            rowSlot += 1
            val s = if (Maff.mod2(rowSlot) == 0) 1 else -1
            val m = s * Maff.div2(rowSlot)
            val angleDelta = angleIncrement * m
            proceed =
              if (angleDelta > Maff.halfPi) {
                nextRow()
              } else if (s < 0 && haltNegative) {
                true
              } else if (s > 0 && haltPositive) {
                true
              } else {
                val p = toFace.radiateRadians(angleCenter + angleDelta, radius)
                if (rowSlot == 0) { arcZone = p.zone }
                if (p.walkable) {
                  p.altitude < minAltitude || p.tile.enemyRangeGround > maxThreat || ! With.grids.formationSlots.tryPlace(unitClass, p)
                } else if ( ! p.walkableTerrain && p.zone != arcZone) {
                  haltNegative ||= s < 0
                  haltPositive ||= s > 0
                  ! haltNegative || ! haltPositive || nextRow()
                } else {
                  true
                }
            }
          }
        }
        j += 1
      }
      i += 1
    }

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

  override def toString: String = f"$group $style from $vanguardCentroid to $goal"
}
