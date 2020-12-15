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

  override def allowed(unit: FriendlyUnitInfo): Boolean = (unit.canMove || unit.canAttack) && unit.agent.canFight
  override protected def perform(unit: FriendlyUnitInfo): Unit = micro(unit, unit.agent.shouldEngage)

  abstract class Technique(val transitions: Technique*) {
    def canTransition(other: Technique): Boolean = transitions.contains(other)
    override val toString: String = getClass.getSimpleName.replace("$", "")
  }

  class MicroContext(val unit: FriendlyUnitInfo, val shouldEngage: Boolean) {
    var technique: Technique = _

    lazy val receivedPushForces: Vector[(Push, Force)] = With.coordinator.pushes
      .get(unit)
      .map(p => (p, p.force(unit)))
      .filter(_._2.exists(_.lengthSquared > 0))
      .map(p => (p._1, p._2.get))
      .toVector
      .sortBy(-_._1.priority.value)
    lazy val receivedPushPriority: TrafficPriority = ByOption.max(receivedPushForces.view.map(_._1.priority)).getOrElse(TrafficPriorities.None)

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
      if (unit.battle.isDefined) unit.positioningWidthTargetCached()
      else if (shouldEngage) unit.agent.destination
      else unit.agent.origin
  }

  def takePotshot(unit: FriendlyUnitInfo): Unit = {
    if (unit.unready) return
    val potshotTarget = Target.best(unit, TargetFilterPotshot)
    unit.agent.toAttack = potshotTarget.orElse(unit.agent.toAttack)
    if (potshotTarget.isDefined && unit.readyForAttackOrder) {
      With.commander.attack(unit)
    }
  }

  def followPushing(context: MicroContext): Unit = {
    if (context.unit.unready) return
    context.unit.agent.escalatePriority(context.receivedPushPriority)
    val force = MicroPathing.getPushRadians(context.unit)
    val towards = force.flatMap(MicroPathing.getWaypointInDirection(context.unit, _))
    if (towards.isDefined) {
      context.unit.agent.toTravel = towards
      With.commander.move(context.unit)
    }
  }

  def aimInPlace(context: MicroContext): Unit = {
    Target.choose(context.unit, TargetFilterVisibleInRange)
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
  object Fallback   extends Technique(Dodge) // Get out of fight while landing shots
  object Organize   extends Technique(Dodge, Excuse, Abuse) // Get into ideal position for future fight
  object Flee       extends Technique(Dodge, Abuse, Fallback, Organize) // Get out of fight
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
      () =>
        unit.agent.canFight
        && context.missingDistanceFromThreat < -64)

    // Evaluate potential attacks
    Target.choose(unit)

    lazy val framesToGetInRangeOfTarget = unit.agent.toAttack.map(unit.framesToGetInRange)
    transition(
      Abuse,
      () => unit.agent.toAttack.exists(canAbuse(unit, _)) && unit.matchups.threats.forall(t => canAbuse(unit, t) || t.framesToGetInRange(unit) > 12 + framesToGetInRangeOfTarget.get))

    transition(
      Fallback,
      () =>
        unit.isAny(Terran.Vulture, Terran.SiegeTankUnsieged, Terran.Goliath, Terran.Wraith, Protoss.Archon, Protoss.Dragoon, Protoss.Reaver, Protoss.Scout, Zerg.Hydralisk, Zerg.Mutalisk)
        && ( unit.is(Protoss.Reaver) || context.receivedPushPriority < TrafficPriorities.Shove)
        && (unit.zone == unit.agent.origin.zone || ! unit.isAny(Protoss.Archon, Protoss.Dragoon) || ! unit.matchups.threats.exists(t => t.is(Protoss.Dragoon) && t.framesToGetInRange(t) < 12)))

    //transition(
    //  Chase,
    //  () => unit.agent.toAttack.exists(t => unit.pixelDistanceSquared(t.pixelCenter.project(t.presumptiveStep, unit.pixelRangeAgainst(t))) > unit.pixelDistanceSquared(t.pixelCenter)))

    transition(Reposition, () => unit.agent.toAttack.map(unit.pixelRangeAgainst).exists(_ > 64))
    transition(Excuse, () => context.receivedPushPriority >= TrafficPriorities.Shove && context.receivedPushPriority > unit.agent.priority)

    if (unit.unready) return

    ///////////////
    // Set goals //
    ///////////////

    def techniqueIs(techniques: Technique*): Boolean = techniques.contains(context.technique)
    val goalPotshot = techniqueIs(Abuse, Fallback, Aim, Organize) || unit.is(Protoss.Reaver)
    val goalHover   = techniqueIs(Abuse, Reposition, Excuse)
    val goalEngage  = techniqueIs(Surround, Chase, Fight, Abuse, Reposition)
    val goalRetreat = techniqueIs(Fallback, Flee, Excuse)
    val goalRegroup = techniqueIs(Organize)

    /////////////
    // Execute //
    /////////////

    // TODO: Move BATTER and other specialized techniques in here
    Brawl.consider(unit)
    if (unit.unready) return

    if (goalPotshot) {
      takePotshot(unit)
      if (unit.unready) return
    }

    // Nudge if we're trying to reach a target
    if (goalEngage && ! unit.flying && unit.agent.toAttack.exists( ! unit.inRangeToAttack(_))) unit.agent.escalatePriority(TrafficPriorities.Nudge)

    // Shove if we're retreating and endangered
    if ((goalHover || goalRetreat) && ! unit.flying) unit.agent.escalatePriority(if (context.tooCloseToThreat) TrafficPriorities.Shove else TrafficPriorities.Bump)

    // TODO: Find smarter firing positions. Find somewhere safe and unoccupied but not too far to stand.
    lazy val firingPosition: Option[Pixel] =
      unit.agent.toAttack
        .map(target => target.pixelCenter.project(unit.pixelCenter, target.unitClass.dimensionMin + unit.unitClass.dimensionMin).nearestTraversablePixel(unit))
        .filter(p => unit.agent.toAttack.exists(t => unit.inRangeToAttack(t, p)))
        .orElse(unit.agent.toTravel)
    unit.agent.toTravel = Some(
      if (goalRegroup)
        context.regroupGoal
      else if (goalRetreat)
        unit.agent.origin
      else firingPosition.getOrElse(unit.agent.destination))

    // Calculate potential forces
    //
    val forces = unit.agent.forces
    val forcesGoal = Seq(Forces.travel, Forces.threat, Forces.leaving)
    val forcesPositioning = Seq(Forces.spacing, Forces.spreading, Forces.cohesion)
    def mul(value: Double, force: Force): Force = force * value
    forces(Forces.travel)     = mul(1, Potential.preferTravel(unit, unit.agent.destination))
    forces(Forces.threat)     = mul(1, Potential.avoidThreats(unit))
    forces(Forces.leaving)    = mul(1, Potential.preferTravel(unit, unit.agent.origin))
    forces(Forces.spacing)    = mul(1, Potential.avoidCollision(unit))
    forces(Forces.spreading)  = mul(1, MicroPathing.getPushRadians(context.receivedPushForces).map(ForceMath.fromRadians(_)).getOrElse(new Force))
    // Verify necessity and improve performance
    //forces(Forces.cohesion)   = mul(1, Potential.preferCohesion(unit))
    // TODO: Splash force
    // TODO: Regroup force -- Proportional to distance from centroid divided by total length of army
    // Reference https://github.com/bmnielsen/Stardust/blob/master/src/General/UnitCluster/Tactics/Move.cpp#L69

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
