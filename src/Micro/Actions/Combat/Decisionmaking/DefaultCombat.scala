package Micro.Actions.Combat.Decisionmaking

import Debugging.ToString
import Debugging.Visualizations.Forces
import Lifecycle.With
import Mathematics.Maff
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Actions.Combat.Tactics.Brawl
import Micro.Actions.Commands.Move
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
    unit.agent.escalatePriority(TrafficPriorities.Shove)
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
  def attackIfReady(unit: FriendlyUnitInfo): Boolean = {
    if (unit.unitClass.ranged && ! unit.readyForAttackOrder) return false
    if (unit.agent.toAttack.forall(unit.pixelsToGetInRange(_) > Math.max(80, unit.topSpeed * With.reaction.agencyMax))) return false
    Commander.attack(unit)
    true
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

    // By now units have already done all non-combat activities
    // So if we have nothing to attack, but are threatened, flee
    lazy val pointlessRisk = unit.matchups.targets.isEmpty && ! unit.matchups.threats.forall(MatchWorker)
    unit.agent.shouldEngage &&= ! pointlessRisk

    //////////////////////
    // Choose technique //
    //////////////////////

    // Evaluate potential attacks -- lazily, because targeting is expensive
    lazy val target = Target.choose(unit)

    var technique: Technique =
      if (unit.agent.shouldEngage && (unit.agent.withinSafetyMargin || unit.matchups.targets.nonEmpty || unit.matchups.threatsInFrames(48).forall(MatchWorker)))
        Fight else Flee

    def transition(newTechnique: Technique, predicate: () => Boolean = () => true, action: () => Unit = () => {}): Unit = {
      if (unit.ready && technique.canTransition(newTechnique) && predicate()) {
        technique = newTechnique
        unit.agent.act(technique.toString)
        action()
      }
    }

    transition(Dodge, () => unit.canMove && unit.agent.receivedPushPriority() >= TrafficPriorities.Dodge, () => dodge(unit))

    lazy val purring = (unit.unitClass.isTerran && unit.unitClass.isMechanical && unit.alliesSquadThenBattle.flatten.exists(a => a.repairing && a.orderTarget.contains(unit)))
    transition(Aim, () => !unit.canMove || purring, () => aim(unit))

    // COG 2021: Disabling Regroup now that we have marching formations
    // transition(Regroup, () => ! unit.agent.shouldEngage && opponentBlocksGoal && unit.agent.withinSafetyMargin)
    // transition(Regroup, () => unit.agent.shouldEngage && opponentBlocksGoal && unit.team.exists(team => ! team.engaged() && unit.confidence() + team.coherence() + team.impatience() / Seconds(35)() < 1))

    lazy val targetAbusable = target.exists(t => ! t.canAttack(unit) || (unit.topSpeed > t.topSpeed && unit.pixelRangeAgainst(t) > t.pixelRangeAgainst(unit)))
    lazy val canKite = target
      .map(unit.framesToGetInRange(_) + unit.unitClass.framesToTurnShootTurnAccelerate + 8) // The flat margin at an edge is a fudge to prevent needlessly brave pokes
      .exists(f => unit.matchups.threats.forall(_.pixelsToGetInRange(unit) > f))
    transition(Abuse, () => unit.unitClass.abuseAllowed && targetAbusable && canKite)

    transition(
      Fallback,
      () =>
        ! unit.agent.shouldEngage
        // Units that can decently attack while retreating
        && unit.unitClass.fallbackAllowed
          && (
          // Tanks/Goliaths can attack while retreating. Reavers can't retreat and their shots are too valuable to waste
          unit.isAny(Terran.SiegeTankUnsieged, Terran.Goliath, Protoss.Reaver)
            || (
            // Don't block anyone on the way out
            unit.agent.receivedPushPriority() < TrafficPriorities.Shove
              // Don't take unnecessary shots in the process
              && unit.matchups
              .threatsInFrames(unit.unitClass.framesToTurnShootTurnAccelerate)
              .forall(_.topSpeed > unit.topSpeed))))

    transition(Dance, () => target.map(unit.pixelRangeAgainst).exists(_ > 64))
    transition(Excuse, () => unit.agent.receivedPushPriority() >= TrafficPriorities.Shove && unit.agent.receivedPushPriority() > unit.agent.priority)

    if (unit.unready) return

    ///////////////
    // Set goals //
    ///////////////

    def techniqueIs(techniques: Technique*): Boolean = techniques.contains(technique)

    val goalPotshot = techniqueIs(Abuse, Fallback, Aim) || Protoss.Reaver(unit)
    val goalDance   = techniqueIs(Abuse, Dance)
    val goalEngage  = techniqueIs(Fight, Abuse, Dance)
    var goalRetreat = techniqueIs(Fallback, Flee, Excuse)
    val goalKeepGap = techniqueIs(Abuse)

    /////////////
    // Execute //
    /////////////

    unit.agent.shouldEngage ||= goalEngage

    // Set traffic priority
    if (goalEngage && target.exists(!unit.inRangeToAttack(_))) unit.agent.escalatePriority(TrafficPriorities.Nudge)
    if (goalDance) unit.agent.escalatePriority(TrafficPriorities.Bump)
    if (goalRetreat) unit.agent.escalatePriority(if (unit.agent.withinSafetyMargin) TrafficPriorities.Bump else TrafficPriorities.Shove)

    // Launch any immediate attacks if we don't need to open a gap
    if ( ! goalKeepGap
      || unit.matchups.pixelsOfEntanglement < 24 - target.map(unit.pixelRangeAgainst).getOrElse(unit.pixelRangeMax)
      || unit.matchups.threats.forall(t => ! t.presumptiveTarget.contains(unit) && t.pixelsToGetInRange(unit) > 64)) {
      if (goalEngage && Brawl.consider(unit)) return
      if (goalPotshot && potshot(unit)) return
      if (goalEngage && attackIfReady(unit)) return
    }

    // Decide where to go
    lazy val pixelToFireFrom = target.map(unit.pixelToFireAt(_, exhaustive = true))
    lazy val pixelFormationAttack = unit.agent.toTravel.filter(d =>
      unit.squad.exists(_.formations.nonEmpty)
      && target.forall(t =>
        unit.pixelDistanceEdge(t, unit.agent.destination) < Math.max(unit.pixelDistanceEdge(t), unit.pixelRangeAgainst(t))
        && (unit.flying
          || d.nearestWalkableTile.tileDistanceGroundManhattan(t.tile.nearestWalkableTile)
          <= unit.tile.nearestWalkableTile.tileDistanceGroundManhattan(t.tile.nearestWalkableTile))))
    unit.agent.toTravel = Some(if (goalRetreat) unit.agent.safety else pixelFormationAttack.orElse(pixelToFireFrom).getOrElse(unit.agent.destination))
    def destination = unit.agent.toTravel.get

    // Avoid the hazards and expense of vector travel when we're not in harm's way
    if (unit.matchups.pixelsOfEntanglement < -320) return

    // Calculate potential forces
    val forces = unit.agent.forces
    var exactDistance: Option[Double] = None
    if (goalRetreat) {
      val originPixelsUs        = unit.pixelDistanceTravelling(unit.agent.safety)
      val originPixelsEnemy     = Maff.minBy(unit.matchups.threats)(_.pixelDistanceEdge(unit)).map(t => t.pixelDistanceTravelling(unit.agent.safety) - t.pixelRangeAgainst(unit)).getOrElse(With.mapPixelPerimeter.toDouble)
      val margin                = 320d
      val marginExit            = originPixelsUs - originPixelsEnemy
      val marginThreat          = unit.matchups.pixelsOfEntanglement
      val ratioExit             = Maff.clamp(marginExit   / margin, -1, 1)
      val ratioThreat           = Maff.clamp(marginThreat / margin, -1, 1)
      forces(Forces.leaving)    = Potential.preferTravel(unit, destination) * (1 + ratioExit)
      forces(Forces.threat)     = Potential.avoidThreats(unit) * (1 + ratioThreat)
      forces(Forces.spacing)    = Potential.avoidCollision(unit)
      forces(Forces.spreading)  = unit.agent.receivedPushForce()
    } else if (goalDance) {
      if (target.isDefined) {
        val distanceIdeal   = idealTargetDistance(unit, target.get)
        val distanceCurrent = unit.pixelDistanceEdge(target.get)
        val distanceTowards = distanceCurrent - distanceIdeal
        val danceForce      = if (distanceTowards > 0) Forces.threat else Forces.travel
        exactDistance = Some(Math.abs(distanceTowards))
        if (pixelFormationAttack.isDefined && distanceTowards > 0) {
          unit.agent.act("FormAttk")
          Commander.move(unit)
        } else if (exactDistance.exists(_ <= 16) || ! target.get.visible) {
          unit.agent.act("Stand")
          Commander.attack(unit)
          return
        } else if (distanceTowards > 0) {
          unit.agent.act("Chase")
          if (unit.inRangeToAttack(target.get) && unit.readyForAttackOrder) {
            Commander.attack(unit)
            return
          }
          val to = target.get.pixel
          val step = target.get.presumptiveStep
          val chaseGoal = if (step.traversableBy(unit) && unit.pixelDistanceSquared(step) >= unit.pixelDistanceSquared(to)) step else to
          val extraChaseDistance = Math.max(0, unit.pixelDistanceCenter(chaseGoal) - unit.pixelDistanceCenter(to))
          unit.agent.toTravel = Some(unit.pixel.project(chaseGoal, distanceTowards + extraChaseDistance))
          Commander.move(unit)
          return
        } else {
          forces(danceForce) = Potential.unitAttraction(unit, target.get, distanceTowards)
          forces(Forces.spreading) = unit.agent.receivedPushForce()
          if (retreat(unit)) {
            unit.agent.act(unit.agent.lastAction.map(_.replaceAll("Retreat", "Kite")).getOrElse("Kite"))
            return
          }
        }
      }
    } else if (goalEngage) {
      forces(Forces.travel)  = Potential.preferTravel(unit, destination)
      forces(Forces.spacing) = Potential.avoidCollision(unit)
    }

    if (goalRetreat && retreat(unit)) return

    val mustApproach = if (goalDance) None else unit.agent.toTravel
    unit.agent.toTravel = MicroPathing.getWaypointInDirection(unit, unit.agent.forces.sum.radians, mustApproach = mustApproach, exactDistance = exactDistance)
    Move.delegate(unit)
  }
}
