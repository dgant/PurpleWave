package Micro.Actions.Combat.Decisionmaking

import Debugging.Visualizations.Forces
import Lifecycle.With
import Mathematics.Physics.{Force, ForceMath}
import Mathematics.Points.Pixel
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Actions.Combat.Tactics.Brawl
import Micro.Actions.Combat.Tactics.Potshot.PotshotTarget
import Micro.Actions.Combat.Targeting.Filters.TargetFilter
import Micro.Actions.Combat.Targeting.{EvaluateTargets, TargetInRange}
import Micro.Coordination.Pathing.MicroPathing
import Micro.Coordination.Pushing.{TrafficPriorities, TrafficPriority}
import Micro.Heuristics.Potential
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.{ByOption, LightYear}

object DefaultCombat extends Action {
  object Engage extends Action {
    override def allowed(unit: FriendlyUnitInfo): Boolean = DefaultCombat.allowed(unit)
    override protected def perform(unit: FriendlyUnitInfo): Unit = DefaultCombat.micro(unit, shouldEngage = true)
  }

  object Disengage extends Action {
    override def allowed(unit: FriendlyUnitInfo): Boolean = DefaultCombat.allowed(unit)
    override protected def perform(unit: FriendlyUnitInfo): Unit = DefaultCombat.micro(unit, shouldEngage = false)
  }

  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.canMove || unit.canAttack
  override protected def perform(unit: FriendlyUnitInfo): Unit = micro(unit, unit.agent.shouldEngage)

  abstract class Technique(val transitions: Technique*) {
    def canTransition(other: Technique): Boolean = {
      transitions.contains(other) || transitions.exists(_.canTransition(other))
    }
    override val toString: String = getClass.getSimpleName
  }

  class MicroContext(val unit: FriendlyUnitInfo, val shouldEngage: Boolean) {
    def retarget(filters: TargetFilter*): Unit = {
      // TODO: Evaluate all targets once and cache values,
      // then pick best target given custom filters
      if (unit.ready) {
        unit.agent.toAttack = EvaluateTargets.best(unit, EvaluateTargets.preferredTargets(unit, filters: _*))
      }
    }

    var technique: Technique = _

    lazy val receivedPushes = With.coordinator.pushes.get(unit).map(p => (p, p.force(unit))).sortBy(-_._1.priority.value)
    lazy val receivedPushForces = receivedPushes.view.filter(_._2.nonEmpty).map(p => (p._1, p._2.get))
    lazy val receivedPushPriority: TrafficPriority = ByOption.max(receivedPushes.view.map(_._1.priority)).getOrElse(TrafficPriorities.None)

    // Decide how far from target/threat we want to be
    lazy val idealPixelsFromTargetRange = if (unit.target.exists(_.speedApproaching(unit) < 0)) -16d else 0d
    lazy val idealPixelsFromThreatRange = 64 + Math.max(0, unit.effectiveRangePixels - ByOption.min(unit.immediateAllies.view.map(_.effectiveRangePixels)).getOrElse(unit.effectiveRangePixels)) / 4 // Induce sorting

    // Decide how far from adjacent teammates we want to be
    lazy val idealPixelsFromTeammatesCollision = if (unit.flying) 0 else unit.unitClass.dimensionMax / 2
    lazy val idealPixelsFromTeammatesSplash = ByOption.max(unit.matchups.threats.view.map(_.unitClass).filter(_.dealsRadialSplashDamage).map(t => 1 + (if (unit.flying) t.airSplashRadius25 else t.groundSplashRadius25))).getOrElse(0)
    lazy val idealPixelsFromTeammates = Math.max(idealPixelsFromTeammatesCollision, idealPixelsFromTeammatesSplash)

    // Check how far from target/threat/teammate we are
    lazy val currentPixelsFromThreatRange = ByOption.min(unit.matchups.threats.view.map(_.pixelsToGetInRange(unit))).getOrElse(LightYear().toDouble)
    lazy val currentPixelsFromTargetRange = unit.target.map(unit.pixelsToGetInRange).getOrElse(unit.pixelDistanceTravelling(unit.agent.destination))
    lazy val currentPixelsFromTeammate = unit.immediateAllies.view.map(_.pixelDistanceEdge(unit))

    lazy val missingDistanceFromThreat = idealPixelsFromThreatRange - currentPixelsFromThreatRange
    lazy val excessDistanceFromTarget = currentPixelsFromTargetRange - idealPixelsFromTargetRange
    lazy val tooCloseToThreat = missingDistanceFromThreat >= 0
    lazy val tooFarFromTarget = excessDistanceFromTarget > 0

    lazy val purring = (unit.unitClass.isTerran && unit.unitClass.isMechanical && unit.immediateAllies.exists(a => a.repairing && a.orderTarget.contains(unit)))

    lazy val regroupGoal: Pixel =
      unit.battle
        .filter(_.us.units.exists(u => u != unit && u.flying == unit.flying))
        .map(battle => if (unit.flying) battle.us.centroidAir else battle.us.centroidGround)
        .getOrElse(if (shouldEngage) unit.agent.destination else unit.agent.origin)
  }

  def takePotshot(context: MicroContext): Boolean = {
    if (context.unit.unready) return true
    val oldToAttack = context.unit.agent.toAttack
    context.unit.agent.toAttack = None
    // TODO: Handle extant targets
    PotshotTarget.delegate(context.unit)
    With.commander.attack(context.unit)
    context.unit.agent.toAttack = context.unit.agent.toAttack.orElse(oldToAttack)
    context.unit.ready
  }

  def followPushing(context: MicroContext): Unit = {
    if (context.unit.unready) return
    context.unit.agent.escalatePriority(context.receivedPushPriority)
    val force = MicroPathing.getPushRadians(context.unit)
    val towards = force.flatMap(MicroPathing.findRayTowards(context.unit, _))
    if (towards.isDefined) {
      context.unit.agent.toTravel = towards
      With.commander.move(context.unit)
    }
  }

  def aimInPlace(context: MicroContext): Unit = {
    TargetInRange.delegate(context.unit)
    With.commander.attack(context.unit)
    With.commander.hold(context.unit)
  }

  object Aim        extends Technique // Stand in place and shoot
  object Dodge      extends Technique // Heed pushes
  object Excuse     extends Technique(Dodge) // Let other units shove through us
  object Surround   extends Technique(Dodge, Excuse) // Pick fight behind target
  object Chase      extends Technique(Dodge, Excuse) // Stay in fight and stay in range
  object Reposition extends Technique(Dodge, Excuse) // Stay in fight with better position
  object Abuse      extends Technique(Dodge, Excuse) // Pick fights from range
  object Fallback   extends Technique(Dodge, Excuse) // Get out of fight while landing shots
  object Organize   extends Technique(Dodge, Excuse, Abuse, Fallback) // Get into ideal position for future fight
  object Flee       extends Technique(Dodge, Excuse, Abuse, Fallback, Organize) // Get out of fight
  object Fight      extends Technique(Dodge, Excuse, Abuse, Surround, Chase, Reposition) // Pick fight ASAP

  @inline final protected def canAbuse(unit: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    ( ! target.canAttack(unit)) || (unit.topSpeed > target.topSpeed && unit.pixelRangeAgainst(target) > target.pixelRangeAgainst(unit))
  }

  protected def micro(unit: FriendlyUnitInfo, shouldEngage: Boolean): Unit = {
    val context = new MicroContext(unit, shouldEngage)

    //////////////////////
    // Choose technique //
    //////////////////////

    def transition(newTechnique: Technique, predicate: () => Boolean = () => true, action: () => Unit = () => {}): Boolean = {
      val doTransition = (context.technique == null || context.technique.canTransition(newTechnique)) && unit.ready && predicate()
      if (doTransition) {
        context.technique = newTechnique
        unit.agent.act(context.technique.toString)
      }
      doTransition
    }

    transition(
      if (shouldEngage) Fight else Flee)

    transition(
      Dodge,
      () => unit.canMove && context.receivedPushPriority >= TrafficPriorities.Dodge,
      () => followPushing(context))

    transition(
      Aim,
      () => ! unit.canMove || context.purring,
      () => aimInPlace(context))

    transition(
      Organize,
      () => context.missingDistanceFromThreat < -64)

    // Evaluate potential attacks
    context.retarget()

    transition(
      Abuse,
      () => unit.agent.toAttack.exists(canAbuse(unit, _)) && unit.matchups.threats.forall(t => canAbuse(unit, t) || t.framesToGetInRange(unit) > 12 + unit.framesToGetInRange(t)))

    transition(
      Fallback,
      () =>
        unit.isAny(Terran.Vulture, Terran.SiegeTankUnsieged, Terran.Goliath, Terran.Wraith, Protoss.Archon, Protoss.Dragoon, Protoss.Reaver, Protoss.Scout, Zerg.Hydralisk, Zerg.Mutalisk)
        && unit.readyForAttackOrder
        && ( unit.is(Protoss.Reaver) || context.receivedPushPriority < TrafficPriorities.Shove)
        && ( ! unit.isAny(Protoss.Archon, Protoss.Dragoon) || ! unit.matchups.threats.exists(t => t.is(Protoss.Dragoon) && t.framesToGetInRange(t) < 12)))

    transition(
      Chase,
      () => unit.agent.toAttack.exists(t => unit.pixelDistanceSquared(t.pixelCenter.project(t.presumptiveStep, unit.pixelRangeAgainst(t))) > unit.pixelDistanceSquared(t.pixelCenter)))

    transition(
      Reposition,
      () => unit.agent.toAttack.map(unit.pixelRangeAgainst).exists(_ > 64))

    transition(
      Excuse,
      () => context.receivedPushPriority >= TrafficPriorities.Shove && context.receivedPushPriority > unit.agent.priority)

    if (unit.unready) return

    ///////////////
    // Set goals //
    ///////////////

    def techniqueIs(techniques: Technique*): Boolean = techniques.contains(context.technique)
    val goalPotshot = techniqueIs(Abuse, Fallback, Aim, Organize)
    val goalHover   = techniqueIs(Abuse, Reposition)
    val goalEngage  = techniqueIs(Surround, Chase, Fight, Abuse, Reposition)
    val goalRetreat = techniqueIs(Fallback, Flee)
    val goalRegroup = techniqueIs(Organize)

    /////////////
    // Execute //
    /////////////

    // TODO: Move BATTER and other specialized techniques in here
    if (Brawl.consider(unit)) return

    if (goalPotshot && takePotshot(context)) return

    // Nudge if we're trying to reach a target
    if (goalEngage && ! unit.flying && unit.agent.toAttack.exists( ! unit.inRangeToAttack(_))) unit.agent.escalatePriority(TrafficPriorities.Nudge)

    // Shove if we're retreating and endangered
    if ((goalHover || goalRetreat) && ! unit.flying) unit.agent.escalatePriority(if (context.tooCloseToThreat) TrafficPriorities.Shove else TrafficPriorities.Bump)

    // TODO: Find smarter firing positions. Find somewhere safe and unoccupied but not too far to stand.
    def firingPosition: Option[Pixel] = unit.agent.toAttack.map(_.pixelCenter).filter(p => unit.flying || p.tileIncluding.walkable).orElse(unit.agent.toTravel)
    unit.agent.toTravel = Some(
      if (goalRegroup)
        context.regroupGoal
      else if (goalRetreat)
        unit.agent.origin
      else firingPosition.getOrElse(unit.agent.destination))

    // Calculate potential forces
    //
    val forces = unit.agent.forces
    val forcesGoal = Seq(Forces.traveling, Forces.threat, Forces.leaving)
    val forcesPositioning = Seq(Forces.spacing, Forces.spreading, Forces.cohesion)
    def mul(value: Double, force: Force): Force = force * value
    forces(Forces.traveling)  = mul(1, Potential.preferTravel(unit, unit.agent.destination))
    forces(Forces.threat)     = mul(1, Potential.avoidThreats(unit))
    forces(Forces.leaving)    = mul(1, Potential.preferTravel(unit, unit.agent.origin))
    forces(Forces.spacing)    = mul(1, Potential.avoidCollision(unit))
    forces(Forces.spreading)  = mul(1, MicroPathing.getPushRadians(context.receivedPushForces).map(ForceMath.fromRadians(_)).getOrElse(new Force))
    forces(Forces.cohesion)   = mul(1, Potential.preferCohesion(unit))
    // TODO: Splash force
    // TODO: Regroup force

    // Reference https://github.com/bmnielsen/Stardust/blob/master/src/General/UnitCluster/Tactics/Move.cpp#L69

    if (goalHover) {
      if (context.tooCloseToThreat) {
        forces(Forces.threat) *= 4
      }
      if (context.tooFarFromTarget) {
        forces(Forces.traveling) *= 2
      }
    } else if (goalRegroup) {
      forces(Forces.traveling) *= 4
    } else if (goalEngage) {
      forces(Forces.traveling) *= 4
    } else if (goalRetreat) {
      forces(Forces.threat) *= 4
    }
    val originFrames = unit.framesToTravelTo(unit.agent.origin)
    if (unit.matchups.threats.forall(threat => threat.framesToTravelPixels(threat.pixelDistanceTravelling(unit.agent.origin) - threat.pixelRangeAgainst(unit)) / threat.topSpeed > originFrames)) {
      forces(Forces.leaving) /= 4
    }

    val maxPositioning = Math.min(1, forcesPositioning.map(forces).map(_.lengthFast).max)
    ForceMath.rebalance(unit.agent.forces, 1.5, forcesGoal: _*)
    ForceMath.rebalance(unit.agent.forces, maxPositioning, forcesPositioning: _*)

    if (goalEngage & unit.agent.toAttack.exists(unit.pixelsToGetInRange(_) < 64) && (unit.unitClass.melee || unit.readyForAttackOrder)) {
      With.commander.attack(unit)
      return
    }

    if (goalRetreat) {
      Retreat.consider(unit)
      return
    }

    // TODO: If we like our position, HOLD

    // TODO: CHASE: Moving shot/pursue if we want to
    val groupTravel = MicroPathing.findRayTowards(unit, unit.agent.forces.sum.radians)

    if (groupTravel.isDefined) {
      unit.agent.toTravel = groupTravel
      With.commander.move(unit)
      return
    }
  }
}