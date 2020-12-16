package Micro.Actions.Combat.Decisionmaking

import Debugging.Visualizations.Forces
import Lifecycle.With
import Mathematics.Physics.{Force, ForceMath}
import Mathematics.Points.Pixel
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Actions.Combat.Tactics.Brawl
import Micro.Actions.Combat.Targeting.Filters.{TargetFilterPotshot, TargetFilterVisibleInRange}
import Micro.Actions.Combat.Targeting.Target
import Micro.Coordination.Pathing.MicroPathing
import Micro.Coordination.Pushing.{Push, TrafficPriorities, TrafficPriority}
import Micro.Heuristics.Potential
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

object DefaultCombat extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = (unit.canMove || unit.canAttack) && unit.agent.canFight

  private abstract class Technique(val transitions: Technique*) {
    def canTransition(other: Technique): Boolean = transitions.contains(other)
    override val toString: String = getClass.getSimpleName.replace("$", "")
  }
  private object Aim        extends Technique // Stand in place and shoot
  private object Dodge      extends Technique // Heed pushes
  private object Excuse     extends Technique(Dodge) // Let other units shove through us
  private object Rally      extends Technique(Dodge, Excuse) // Catch up with the fight (TODO)
  private object Reposition extends Technique(Dodge, Excuse) // Stay in fight with better position
  private object Abuse      extends Technique(Dodge, Excuse) // Pick fights from range
  private object Fallback   extends Technique(Dodge) // Get out of fight while landing shots
  private object Regroup    extends Technique(Dodge, Excuse, Abuse) // Get into ideal position for future fight
  private object Flee       extends Technique(Dodge, Abuse, Fallback, Regroup) // Get out of fight
  private object Fight      extends Technique(Dodge, Excuse, Abuse, Reposition, Flee) // Pick fight ASAP

  @inline final protected def canAbuse(unit: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    ( ! target.canAttack(unit)) || (unit.topSpeed > target.topSpeed && unit.pixelRangeAgainst(target) > target.pixelRangeAgainst(unit))
  }

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    var technique: Technique = if (unit.agent.shouldEngage) Fight else Flee

    lazy val receivedPushForces: Vector[(Push, Force)] = With.coordinator.pushes
      .get(unit)
      .map(p => (p, p.force(unit)))
      .filter(_._2.exists(_.lengthSquared > 0))
      .map(p => (p._1, p._2.get))
      .toVector
      .sortBy(-_._1.priority.value)
    lazy val receivedPushPriority: TrafficPriority = ByOption.max(receivedPushForces.view.map(_._1.priority)).getOrElse(TrafficPriorities.None)

    lazy val idealPixelsFromThreatRange = 64 + Math.max(0, unit.effectiveRangePixels - ByOption.min(unit.immediateAllies.view.map(_.effectiveRangePixels)).getOrElse(unit.effectiveRangePixels)) / 4 // Induce sorting
    lazy val missingDistanceFromThreat = idealPixelsFromThreatRange - unit.matchups.pixelsOfEntanglement
    lazy val tooCloseToThreat = missingDistanceFromThreat >= 0

    lazy val purring = (unit.unitClass.isTerran && unit.unitClass.isMechanical && unit.immediateAllies.exists(a => a.repairing && a.orderTarget.contains(unit)))

    lazy val regroupGoal: Pixel =
      if (unit.battle.isDefined) unit.positioningWidthTargetCached()
      else if (unit.agent.shouldEngage) unit.agent.destination
      else unit.agent.origin

    def potshot(): Unit = {
      if (unit.unready) return
      val potshotTarget = Target.best(unit, TargetFilterPotshot)
      unit.agent.toAttack = potshotTarget.orElse(unit.agent.toAttack)
      if (potshotTarget.isDefined && unit.readyForAttackOrder) {
        With.commander.attack(unit)
      }
    }
    def dodge(): Unit = {
      if (unit.unready) return
      unit.agent.escalatePriority(receivedPushPriority)
      val towards = MicroPathing.getPushRadians(unit).flatMap(MicroPathing.getWaypointInDirection(unit, _))
      if (towards.isDefined) {
        unit.agent.toTravel = towards
        With.commander.move(unit)
      }
    }
    def aim(): Unit = {
      if (unit.unready) return
      Target.choose(unit, TargetFilterVisibleInRange)
      With.commander.attack(unit)
      With.commander.hold(unit)
    }

    //////////////////////
    // Choose technique //
    //////////////////////

    def transition(newTechnique: Technique, predicate: () => Boolean = () => true, action: () => Unit = () => {}): Boolean = {
      val doTransition = (technique == null || technique.canTransition(newTechnique)) && unit.ready && predicate()
      if (doTransition) {
        technique = newTechnique
        unit.agent.act(technique.toString)
      }
      doTransition
    }

    transition(Dodge, () => unit.canMove && receivedPushPriority >= TrafficPriorities.Dodge, () => dodge())
    transition(Aim, () => ! unit.canMove || purring, () => aim())
    transition(Regroup, () => ! unit.agent.shouldEngage && missingDistanceFromThreat < -64)
    transition(Regroup, () =>   unit.agent.shouldEngage && unit.battle.map(_.us).exists(team => ! team.engaged() && team.coherence() < unit.confidence))

    // Evaluate potential attacks
    Target.choose(unit)

    transition(
      Flee,
      () => unit.agent.toAttack.isEmpty && unit.matchups.pixelsOfEntanglement > -128)

    lazy val framesToGetInRangeOfTarget = unit.agent.toAttack.map(unit.framesToGetInRange)
    transition(
      Abuse,
      () => unit.agent.toAttack.exists(canAbuse(unit, _)) && unit.matchups.threats.forall(t => canAbuse(unit, t) || t.framesToGetInRange(unit) > 12 + framesToGetInRangeOfTarget.get))

    transition(
      Fallback,
      () =>
        unit.isAny(Terran.Vulture, Terran.SiegeTankUnsieged, Terran.Goliath, Terran.Wraith, Protoss.Archon, Protoss.Dragoon, Protoss.Reaver, Protoss.Scout, Zerg.Hydralisk, Zerg.Mutalisk)
        && ( unit.is(Protoss.Reaver) || receivedPushPriority < TrafficPriorities.Shove)
        && (unit.zone == unit.agent.origin.zone || ! unit.isAny(Protoss.Archon, Protoss.Dragoon) || ! unit.matchups.threats.exists(t => t.is(Protoss.Dragoon) && t.framesToGetInRange(t) < 12)))

    transition(Reposition, () => unit.agent.toAttack.map(unit.pixelRangeAgainst).exists(_ > 64))

    // TODO: Reenable once we stop shoving so needlessly
    //transition(Excuse, () => receivedPushPriority >= TrafficPriorities.Shove && receivedPushPriority > unit.agent.priority)

    if (unit.unready) return

    ///////////////
    // Set goals //
    ///////////////

    def techniqueIs(techniques: Technique*): Boolean = techniques.contains(technique)
    val goalPotshot = techniqueIs(Abuse, Fallback, Aim, Regroup) || unit.is(Protoss.Reaver)
    val goalHover   = techniqueIs(Abuse, Reposition, Excuse)
    val goalEngage  = techniqueIs(Fight, Abuse, Reposition)
    val goalRetreat = techniqueIs(Fallback, Flee, Excuse)
    val goalRegroup = techniqueIs(Regroup)

    /////////////
    // Execute //
    /////////////

    // TODO: Move BATTER and other specialized techniques in here
    Brawl.consider(unit)
    if (unit.unready) return

    if (goalPotshot) {
      potshot()
      if (unit.unready) return
    }

    // Nudge if we're trying to reach a target
    if (goalEngage && ! unit.flying && unit.agent.toAttack.exists( ! unit.inRangeToAttack(_))) unit.agent.escalatePriority(TrafficPriorities.Nudge)

    // Shove if we're retreating and endangered
    if ((goalHover || goalRetreat) && ! unit.flying) unit.agent.escalatePriority(if (tooCloseToThreat) TrafficPriorities.Shove else TrafficPriorities.Bump)

    // TODO: Find smarter firing positions. Find somewhere safe and unoccupied but not too far to stand.
    lazy val firingPosition: Option[Pixel] =
      unit.agent.toAttack
        .map(target => target.pixelCenter.project(unit.pixelCenter, target.unitClass.dimensionMin + unit.unitClass.dimensionMin).nearestTraversablePixel(unit))
        .filter(p => unit.agent.toAttack.exists(t => ! t.flying || unit.inRangeToAttack(t, p)))
        .orElse(unit.agent.toTravel)
    unit.agent.toTravel = Some(
      if (goalRegroup)
        regroupGoal
      else if (goalRetreat)
        unit.agent.origin
      else firingPosition.getOrElse(unit.agent.destination))

    // Calculate potential forces
    val forces = unit.agent.forces
    val forcesGoal = Seq(Forces.travel, Forces.threat, Forces.leaving)
    val forcesPositioning = Seq(Forces.spacing, Forces.spreading, Forces.cohesion)
    def mul(value: Double, force: Force): Force = force * value
    forces(Forces.travel)     = mul(1, Potential.preferTravel(unit, unit.agent.destination))
    forces(Forces.threat)     = mul(1, Potential.avoidThreats(unit))
    forces(Forces.leaving)    = mul(1, Potential.preferTravel(unit, unit.agent.origin))
    forces(Forces.spacing)    = mul(1, Potential.avoidCollision(unit))
    forces(Forces.spreading)  = mul(1, MicroPathing.getPushRadians(receivedPushForces).map(ForceMath.fromRadians(_)).getOrElse(new Force))

    if (goalHover) {
      val closeEnough = unit.agent.toAttack.forall(unit.inRangeToAttack)
      val exposed = unit.matchups.threatsInRange.exists(t => t.inRangeToAttack(unit) && ! unit.agent.toAttack.contains(t))
      if (closeEnough) {
        if (exposed) {
          forces(Forces.threat) *= 3
        }
      } else {
        forces(Forces.travel) *= 3
      }
    } else if (goalRegroup) {
      forces(Forces.travel) *= 3
    } else if (goalEngage) {
      forces(Forces.travel) *= 3
    } else if (goalRetreat) {
      forces(Forces.threat) *= 2
      forces(Forces.leaving) *= 2
    }
    val originFrames = unit.framesToTravelTo(unit.agent.origin)
    if (unit.matchups.threats.forall(threat => threat.framesToTravelPixels(threat.pixelDistanceTravelling(unit.agent.origin) - threat.pixelRangeAgainst(unit)) / threat.topSpeed > originFrames)) {
      forces(Forces.leaving) /= 4
    }

    val weighGoal = if (goalEngage || goalRetreat) 1.5 else 0.75
    val weighPositioning = Math.min(1, forcesPositioning.map(forces).map(_.lengthFast).max)
    ForceMath.rebalance(unit.agent.forces, weighGoal, forcesGoal: _*)
    ForceMath.rebalance(unit.agent.forces, weighPositioning, forcesPositioning: _*)

    if (goalEngage
      && (unit.unitClass.melee || unit.readyForAttackOrder) // Don't want to dance
      && unit.agent.toAttack.exists(unit.pixelsToGetInRange(_) < Math.max(64, unit.topSpeed * With.reaction.agencyMax))) {
      With.commander.attack(unit)
      return
    }

    if (goalRetreat) {
      Retreat.consider(unit)
      return
    }

    // Avoid the hazards of vector travel when we're not crossing the map
    if (unit.battle.isEmpty && unit.agent.destination.zone == unit.agent.origin.zone) {
      return
    }

    // TODO: CHASE: Moving shot/pursue if we want to
    val groupTravelGoal = unit.agent.toTravel.filter(goal => ! goalHover && ! goalRegroup)
    val groupTravelWaypoint = MicroPathing.getWaypointInDirection(unit, unit.agent.forces.sum.radians, mustApproach = groupTravelGoal)
    if (groupTravelWaypoint.isDefined) {
      unit.agent.toTravel = groupTravelWaypoint
      With.commander.move(unit)
      return
    }
  }
}
