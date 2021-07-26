package Micro.Actions.Combat.Decisionmaking

import Debugging.ToString
import Debugging.Visualizations.Forces
import Lifecycle.With
import Mathematics.Maff
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Actions.Combat.Tactics.Brawl
import Micro.Agency.Commander
import Micro.Coordination.Pathing.MicroPathing
import Micro.Coordination.Pushing.TrafficPriorities
import Micro.Heuristics.Potential
import Micro.Targeting.FiltersSituational.{TargetFilterPotshot, TargetFilterVisibleInRange}
import Micro.Targeting.Target
import Planning.UnitMatchers.MatchWorker
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object DefaultCombat extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = (unit.canMove || unit.canAttack) && unit.intent.canFight

  private abstract class Technique(val transitions: Technique*) {
    def canTransition(other: Technique): Boolean = transitions.contains(other)
    override val toString: String = ToString(this)
  }
  private object Aim        extends Technique // Stand in place and shoot
  private object Dodge      extends Technique // Heed pushes
  private object Excuse     extends Technique(Dodge) // Let other units shove through us
  private object Dance      extends Technique(Dodge, Excuse) // Stay in fight with better position
  private object Abuse      extends Technique(Dodge, Excuse) // Pick fights from range
  private object Fallback   extends Technique(Dodge) // Get out of fight while landing shots
  private object Flee       extends Technique(Dodge, Abuse, Fallback) // Get out of fight
  private object Fight      extends Technique(Dodge, Abuse, Excuse, Dance, Flee) // Pick fight ASAP

  def potshot(unit: FriendlyUnitInfo): Boolean = {
    if (unit.unready) return false
    if ( ! unit.readyForAttackOrder) return false
    val potshotTarget = unit.agent.toAttack.filter(TargetFilterPotshot.legal(unit, _)).orElse(Target.best(unit, TargetFilterPotshot))
    unit.agent.toAttack = potshotTarget.orElse(unit.agent.toAttack)
    if (potshotTarget.isDefined) {
      unit.agent.act("Potshot")
      Commander.attack(unit)
    }
    unit.unready
  }
  def dodge(unit: FriendlyUnitInfo): Boolean = {
    if (unit.unready) return false
    unit.agent.escalatePriority(TrafficPriorities.Shove) // Just lower than the dodge itself
    val towards = MicroPathing.getPushRadians(unit).flatMap(MicroPathing.getWaypointInDirection(unit, _))
    if (towards.isDefined) {
      unit.agent.toTravel = towards
      Commander.move(unit)
    }
    unit.unready
  }
  def aim(unit: FriendlyUnitInfo): Boolean = {
    if (unit.unready) return false
    Target.choose(unit, TargetFilterVisibleInRange)
    Commander.attack(unit)
    Commander.hold(unit)
    unit.unready
  }
  def readyToAttackTarget(unit: FriendlyUnitInfo): Boolean = {
    if (unit.agent.toAttack.isEmpty) return false
    if (unit.unitClass.ranged && unit.framesToGetInRange(unit.agent.toAttack.get) + 4 < unit.cooldownLeft) return false
    true
  }
  def attackIfReady(unit: FriendlyUnitInfo): Boolean = {
    if (readyToAttackTarget(unit)) {  Commander.attack(unit) }
    unit.ready
  }
  def retreat(unit: FriendlyUnitInfo): Boolean = {
    // Retreat, but potshot if we're trapped
    val retreat = Retreat.getRetreat(unit)
    if (unit.unitClass.fallbackAllowed && ! unit.loaded) {
      val retreatDistance = unit.pixelDistanceCenter(retreat.to)
      val retreatStep = unit.pixel.project(retreat.to, Math.min(retreatDistance, 40 + Math.max(0, unit.matchups.pixelsOfEntanglement)))
      if (unit.matchups.threats.exists(threat => threat.inRangeToAttack(unit, retreatStep))) {
        if (potshot(unit)) return true
      }
    }
    Retreat.applyRetreat(retreat)
    true
  }

  def idealTargetDistance(unit: FriendlyUnitInfo, target: UnitInfo): Double = {
    lazy val distance           = unit.pixelDistanceEdge(target)
    lazy val range              = unit.pixelRangeAgainst(target)
    lazy val rangeAgainstUs     = if (target.canAttack(unit)) Some(target.pixelRangeAgainst(unit)) else None
    lazy val rangeEqual         = rangeAgainstUs.contains(range)
    lazy val pixelsOutranged    = rangeAgainstUs.map(_ - unit.pixelRangeAgainst(target)).filter(_ > 0)
    lazy val confidentToChase   = unit.confidence() > .25
    lazy val projectedUs        = unit.pixel.projectUpTo(target.pixel, 16)
    lazy val projectedTarget    = target.pixel.projectUpTo(target.presumptiveStep, 16)
    lazy val targetApproaching  = unit.pixelDistanceSquared(target) > unit.pixelDistanceSquared(projectedTarget)
    lazy val targetEscaping     = unit.pixelDistanceSquared(target) < unit.pixelDistanceSquared(projectedTarget)
    lazy val inChoke            = ! unit.flying && unit.zone.edges.exists(_.contains(unit.pixel))
    lazy val nudgedTowards      = ! unit.flying && target.pixelDistanceSquared(unit.pixel.add(unit.agent.receivedPushForce().normalize(distance / 2).toPoint)) < unit.pixelDistanceSquared(target)
    lazy val sameThreatsChasing = unit.matchups.threats.forall(t => t.inRangeToAttack(unit) == t.inRangeToAttack(unit, projectedUs))
    lazy val obiwanned          = ! unit.flying && ! target.flying && ! unit.unitClass.melee && target.altitude > unit.altitude
    lazy val chaseDistance      = rangeAgainstUs.filter(_ < range).map(r => Math.max(24, (range - r) / 2)).getOrElse(0d)

    if ( ! target.canMove && ! target.canAttack(unit)) {
      return if (inChoke && nudgedTowards) 0 else range
    }

    // Chasing behaviors
    // Reavers shouldn't even try
    if (Protoss.Reaver(unit)) return range

    // Chase if they outrange us
    if (pixelsOutranged.isDefined) return chaseDistance

    // Chase if we're in a choke and being nudged towards them
    if (inChoke && nudgedTowards) return chaseDistance

    // Chase if we're confident and fighting up high ground
    if (obiwanned) return chaseDistance

    // Chase if we're confident and being nudged towards them
    if (confidentToChase && nudgedTowards) return chaseDistance

    // Chase if we're confident and they're about to escape us
    if (confidentToChase && targetEscaping) return chaseDistance

    // Chase if they're escaping out of our ideal range and chasing won't bring us into range of anyone new
    if (targetEscaping && sameThreatsChasing) return chaseDistance

    // Breathe if range is equal and they shoot first
    if (rangeEqual && target.cooldownLeft < unit.cooldownLeft) return range + Math.max(0, unit.cooldownLeft - With.latency.latencyFrames - unit.framesToTurnTo(target)) * unit.topSpeed

    // Maintain maximum range if chasing puts us in range of anyone new
    if ( ! sameThreatsChasing) return range

    // Maintain maximum range if they're a threat coming towards us
    if (targetApproaching) return range

    // Maintain just enough range if we're being nudged
    if (nudgedTowards) return chaseDistance

    // Otherwise, stay near max range. Get a little closer if we can in case they try to flee
    rangeAgainstUs.map(rangeAgainst => Maff.clamp(rangeAgainst + 64, range - 16, range)).getOrElse(range - 16)
  }

  override protected def perform(unit: FriendlyUnitInfo): Unit = {

    lazy val target = Target.choose(unit)
    unit.agent.shouldEngage &&= target.nonEmpty || unit.matchups.threats.forall(MatchWorker) || unit.matchups.targets.nonEmpty

    //////////////////////////
    // Set traffic priority //
    //////////////////////////

    if (target.exists( ! unit.inRangeToAttack(_))) unit.agent.escalatePriority(TrafficPriorities.Nudge)
    if ( ! unit.agent.shouldEngage) {
      if (unit.matchups.pixelsOfEntanglement > -96) {
        unit.agent.escalatePriority(TrafficPriorities.Shove)
      } else if (unit.matchups.pixelsOfEntanglement > -160) {
        unit.agent.escalatePriority(TrafficPriorities.Bump)
      }
    }

    //////////////////////
    // Choose technique //
    //////////////////////

    var technique: Technique =
      if ((unit.agent.shouldEngage || unit.battle.isEmpty) && (unit.agent.withinSafetyMargin || unit.matchups.targets.nonEmpty || unit.matchups.threatsInFrames(48).forall(MatchWorker)))
        Fight else Flee

    def transition(newTechnique: Technique, predicate: () => Boolean = () => true, action: () => Unit = () => {}): Unit = {
      if (unit.ready && technique.canTransition(newTechnique) && predicate()) {
        technique = newTechnique
        unit.agent.act(technique.toString)
        action()
      }
    }

    lazy val purring            = unit.unitClass.isTerran && unit.unitClass.isMechanical && unit.alliesSquadThenBattle.flatten.exists(a => a.repairing && a.orderTarget.contains(unit))
    lazy val canAbuseTarget     = target.exists(t => ! t.canAttack(unit) || (unit.topSpeed > t.topSpeed && unit.pixelRangeAgainst(t) > t.pixelRangeAgainst(unit)))
    lazy val framesToPokeTarget = target.map(unit.framesToGetInRange(_) + unit.unitClass.framesToTurnShootTurnAccelerate)
    lazy val hasTimeToPoke      = framesToPokeTarget.exists(framesToPoke => unit.matchups.threats.forall(_.framesToGetInRange(unit) > framesToPoke + 8))

    transition(Aim,       () => ! unit.canMove, () => aim(unit))
    transition(Dodge,     () => unit.agent.receivedPushPriority() >= TrafficPriorities.Dodge, () => dodge(unit))
    transition(Aim,       () => purring, () => aim(unit))
    transition(Abuse,     () => unit.unitClass.abuseAllowed && canAbuseTarget && hasTimeToPoke)
    transition(Fallback,  () => unit.unitClass.fallbackAllowed && (
      // Only Fallback if our shots are too valuable to waste or we can do it without endangering allies
      unit.isAny(Terran.SiegeTankUnsieged, Terran.Goliath, Protoss.Reaver)
      || (
        unit.agent.receivedPushPriority() < TrafficPriorities.Shove
        && unit.matchups.threatsInFrames(unit.unitClass.framesToTurnShootTurnAccelerate).forall(_.topSpeed > unit.topSpeed))))
    transition(Dance,     () => unit.unitClass.danceAllowed && target.map(unit.pixelRangeAgainst).exists(_ > 64))
    //transition(Excuse,    () => unit.agent.receivedPushPriority() > unit.agent.priority)

    if (unit.unready) return

    ///////////////
    // Set goals //
    ///////////////

    def techniqueIs(techniques: Technique*): Boolean = techniques.contains(technique)

    val goalPotshot = techniqueIs(Abuse, Fallback) || Protoss.Reaver(unit)
    val goalDance   = techniqueIs(Abuse, Dance)
    val goalEngage  = techniqueIs(Fight, Abuse, Dance)
    val goalRetreat = techniqueIs(Fallback, Flee, Excuse)

    /////////////
    // Execute //
    /////////////

    unit.agent.shouldEngage ||= goalEngage
    if (goalRetreat) unit.agent.toTravel = Some(unit.agent.safety)

    val breakFormationThreshold = 64
    lazy val targetDistanceHere = unit.pixelDistanceEdge(target.get)
    lazy val targetDistanceThere = unit.pixelDistanceEdgeFrom(target.get, unit.agent.destination)
    lazy val formationHelpsEngage = targetDistanceThere <= Math.min(targetDistanceHere, unit.pixelRangeAgainst(target.get))
    lazy val breakFormationToAttack = unit.squad.forall(_.formations.isEmpty) || target.exists(targ =>
        // If we're not ready to attack yet, just slide into formation
        (readyToAttackTarget(unit) || unit.unitClass.melee) && (
          // Break if we are already in range
          unit.inRangeToAttack(targ)
          // Break if we are closer to range than the formation, and already pretty close
          || targetDistanceHere < Math.min(targetDistanceThere, 32 * 8)
          // Break if the fight has already begun and the formation isn't helping us
          || (unit.team.exists(_.engagedUpon) && ! formationHelpsEngage)))
    if (goalEngage && Brawl.consider(unit)) return
    if (goalPotshot && potshot(unit)) return
    if (goalEngage && breakFormationToAttack && attackIfReady(unit)) return

    if (goalRetreat) {
      retreat(unit)
      return
    } else if (goalDance) {
      val distanceIdeal   = idealTargetDistance(unit, target.get)
      val distanceCurrent = unit.pixelDistanceEdge(target.get)
      val distanceTowards = distanceCurrent - distanceIdeal
      if (distanceTowards >= 0) {
        if (breakFormationToAttack) {
          if (distanceTowards >= 32) {
            unit.agent.act("Chase")
            val to                  = target.get.pixel
            val step                = target.get.presumptiveStep
            val chaseGoal           = if (step.traversableBy(unit) && unit.pixelDistanceSquared(step) >= unit.pixelDistanceSquared(to)) step else to
            val extraChaseDistance  = Math.max(0, unit.pixelDistanceCenter(chaseGoal) - unit.pixelDistanceCenter(to))
            unit.agent.toTravel     = Some(unit.pixel.project(chaseGoal, distanceTowards + extraChaseDistance))
            Commander.move(unit)
            return
          } else {
            unit.agent.act("Charge")
            Commander.attack(unit)
            return
          }
        } else {
          Commander.move(unit)
          return
        }
      } else {
        unit.agent.forces(Forces.threat) = Potential.avoidThreats(unit)
        unit.agent.forces(Forces.spacing) = unit.agent.receivedPushForce()
        retreat(unit)
        unit.agent.act(unit.agent.lastAction.map(_.replaceAll("Retreat", "Kite")).getOrElse("Kite"))
        return
      }
    }
    Commander.move(unit)
  }
}
