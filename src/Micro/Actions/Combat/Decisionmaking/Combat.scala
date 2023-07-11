package Micro.Actions.Combat.Decisionmaking

import Debugging.Visualizations.{ForceLabel, Forces}
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Physics.Force
import Micro.Actions.Action
import Micro.Actions.Combat.Decisionmaking.Techniques._
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Actions.Combat.Tactics.Brawl
import Micro.Actions.Protoss.BeReaver
import Micro.Agency.Commander
import Micro.Coordination.Pathing.MicroPathing
import Micro.Coordination.Pushing.TrafficPriorities
import Micro.Formation.Formation
import Micro.Heuristics.Potential
import Micro.Targeting.FiltersSituational.{TargetFilterPotshot, TargetFilterVisibleInRange}
import Micro.Targeting.Target
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Tactic.Squads.{GenericFriendlyUnitGroup, TFriendlyUnitGroup, UnitGroup}
import Utilities.Time.{Forever, Seconds}
import Utilities.UnitFilters.{IsSpeedling, IsSpeedlot}
import Utilities.{?, SwapIf}

final class Combat(unit: FriendlyUnitInfo) extends Action {

  var restrained            : Boolean = false
  var restrainedFrames      : Double  = 0.0
  var technique             : Technique = Fight
  var framesUntilShot       : Int = Forever()
  var framesToPokeTarget    : Option[Int] = None
  var idealTargetDistance   : Double = _
  var hasSpacetimeToPoke    : Boolean = _
  var breakFormationToAttack: Boolean = _
  var trapped               : Boolean = _

  protected def group                 : UnitGroup with TFriendlyUnitGroup = unit.squad.orElse(unit.friendlyTeam).getOrElse(new GenericFriendlyUnitGroup(unit))
  protected def target                : Option[UnitInfo]  = unit.agent.toAttack
  protected def formations            : Seq[Formation]    = unit.squad.map(_.formations).getOrElse(Seq.empty)
  protected def engageFormation       : Option[Formation] = formations.dropRight(1).headOption.filter(_.placements.contains(unit))
  protected def squadEngaged          : Boolean = unit.squad.map(_.engagedUpon).getOrElse(unit.matchups.engagedUpon)
  protected def fightConsensus        : Boolean = unit.squad.map(_.fightConsensus).getOrElse(unit.agent.shouldFight)
  protected def assembling            : Boolean = false && engageFormation.isDefined && ! group.engagedUpon && Math.max(0.25, group.confidence11) < 0.95 - group.restrainedFrames / Seconds(10)()
  protected def targetFleeing         : Boolean = target.exists(t => t.canMove && Maff.isTowards(unit.pixel, t.pixel, t.angleRadians))
  protected def targetDistanceHere    : Double  = target.map(unit.pixelDistanceEdge).getOrElse(0d)
  protected def targetDistanceThere   : Double  = target.map(unit.pixelDistanceEdgeFrom(_, unit.agent.destination)).getOrElse(0d)
  protected def formationHelpsEngage  : Boolean = targetDistanceThere <= Math.min(targetDistanceHere, target.map(unit.pixelRangeAgainst).getOrElse(0d))
  protected def formationHelpsChase   : Boolean = targetDistanceThere <= targetDistanceHere
  protected def idealDistanceForward  : Double  = target.map(unit.pixelDistanceEdge(_) - idealTargetDistance).getOrElse(0d)
  protected def confidentEnoughToChase: Boolean = unit.confidence11 > confidenceChaseThreshold
  protected def shouldChase           : Boolean = (idealDistanceForward > 0 && confidentEnoughToChase && targetFleeing) || (target.exists(_.isAny(Terran.SiegeTankSieged, Protoss.Reaver)) && ! unit.flying && ! Protoss.Reaver(unit))
  protected def allThreatsAbusable    : Boolean = unit.matchups.threats.forall(canAbuse)
  protected def nudged                : Boolean = unit.agent.receivedPushPriority() > TrafficPriorities.Nudge
  protected def nudgedTowards         : Boolean = nudged && Maff.isTowards(unit.pixel, unit.agent.destination, unit.agent.receivedPushForce().radians)
  protected def pushThrough           : Boolean = shouldEngage && engageFormation.isDefined && nudged && nudgedTowards && ! unit.flying
  protected def ground10              : Double  = ?(unit.flying, 0, 1)
  protected def abroad10              : Double  = ?(unit.zone.owner.isUs, 0, 1)
  protected def shouldRetreat         : Boolean = techniqueIs(Fallback, Flee, Excuse)
  protected def shouldEngage          : Boolean = techniqueIs(Fight, Abuse, Scavenge)
  protected def canAbuse(t: UnitInfo) : Boolean = unit.canAttack(t) && ( ! t.canAttack(unit) || (unit.topSpeed > t.topSpeed && unit.pixelRangeAgainst(t) > t.pixelRangeAgainst(unit)))

  override def allowed(unit: FriendlyUnitInfo): Boolean = (unit.canMove || unit.canAttack) && unit.intent.canFight
  override def perform(unused: FriendlyUnitInfo = null): Unit = {
    restrained = false
    innerPerform()
    restrainedFrames += ?(restrained, 1.0, -0.5) * With.framesSince(unit.agent.lastFrame)
    restrainedFrames = Math.max(0, restrainedFrames)
  }
  def innerPerform(): Unit = {
    Target.choose(unit)
    unit.agent.chooseAttackFrom()
    Commander.defaultEscalation(unit)

    framesUntilShot       = Maff.min((Seq(unit.pixel) ++ unit.agent.toAttackFrom).view.flatMap(p =>  unit.matchups.threats.map(_.framesToGetInRange(unit, p)))).getOrElse(Forever())
    framesToPokeTarget    = target.map(unit.framesToLaunchAttack(_) + unit.unitClass.framesToPotshot + With.reaction.agencyMax + With.latency.latencyFrames)
    idealTargetDistance   = getIdealTargetDistance
    hasSpacetimeToPoke    = framesToPokeTarget.exists(_ < framesUntilShot) && unit.agent.toAttackFrom.forall(p => ! unit.matchups.threats.exists(t => t.inRangeToAttack(unit, p.project(t.pixel, 16))))
    lazy val safePassage  = unit.matchups.ignorant || unit.topSpeedTransported >= 0.9 * group.meanTopSpeed && unit.matchups.pixelsToThreatRange.forall(_ > 32 * ?(unit.agent.receivedPushPriority() < TrafficPriorities.Bump, 2, 16))

    technique =
      if ( ! unit.canMove)                                  Fight
      else if (unit.agent.shouldFight && target.isDefined)  Fight
      else if (safePassage)                                 Walk
      else                                                  Flee

    transition(Aim,       ! unit.canMove)
    transition(Dodge,     unit.agent.receivedPushPriority() >= TrafficPriorities.Dodge)
    transition(Abuse,     unit.unitClass.abuseAllowed && unit.matchups.targetNearest.exists(canAbuse) && (hasSpacetimeToPoke || allThreatsAbusable) && (shouldRetreat || framesUntilShot < unit.cooldownMaxAirGround))
    transition(Scavenge,
      target.exists(_.matchups.framesToLive > unit.matchups.framesToLive)
      && unit.matchups.threatDeepest.exists(t => unit.canAttack(t) && unit.pixelRangeAgainst(t) >= t.pixelRangeAgainst(unit))
      && unit.totalHealth * 2.0 <= group.meanAttackerHealth
      && unit.totalHealth * 3.0 <= unit.matchups.threatsInPixels(96).map(_.damageOnNextHitAgainst(unit)).sum)
    transition(Fallback,  unit.isAny(Terran.SiegeTankUnsieged, Terran.Goliath, Protoss.Reaver))
    transition(Fallback,
      Protoss.Dragoon(unit)
      && unit.agent.receivedPushPriority() < TrafficPriorities.Shove
      && unit.matchups.targetNearest.exists(unit.inRangeToAttack)
      && unit.matchups.threatsInFrames(unit.unitClass.framesToPotshot + 9).forall(_.isAny(Terran.Vulture, IsSpeedlot, Zerg.Zergling)))
    transition(Fallback,  unit.isAny(Protoss.Archon, Protoss.Zealot) && unit.matchups.targetsInRange.exists(IsSpeedling))

    if (shouldEngage && ! unit.agent.shouldFight) {
      unit.agent.shouldFight = true
      unit.agent.fightReason = "Technique"
    }
    if (shouldRetreat) {
      unit.agent.toTravel = Some(unit.agent.safety)
      if (unit.agent.shouldFight) {
        unit.agent.shouldFight = false
        unit.agent.fightReason = "Technique"
      }
    }

    if (technique == Walk     && walk())        return
    if (technique == Aim      && aim())         return
    if (technique == Dodge    && dodge())       return
    if (technique == Fallback && potshot())     return
    if (shouldRetreat         && Retreat(unit)) return
    if (shouldEngage          && Brawl(unit))   return
    if (technique == Abuse    && abuse())       return
    if (shouldEngage          && engage())      {}
  }

  ////////////////
  // Techniques //
  ////////////////

  protected def techniqueIs(techniques: Technique*): Boolean = techniques.contains(technique)
  protected def transition(newTechnique: Technique, predicate: => Boolean): Unit = {
    if (unit.ready && technique.canTransition(newTechnique) && predicate) {
      technique = newTechnique
      unit.agent.act(technique.toString)
    }
  }

  ///////////////
  // Potential //
  ///////////////

  protected def applyForce(label: ForceLabel, force: Force): Unit = {
    if (force.lengthFast > 0) {
      unit.agent.forces.put(label, force)
    }
  }
  protected def applySeparationForces(): Unit = {
    applyForce(Forces.spacing,  Potential.preferSpacing(unit))
    applyForce(Forces.pushing,  Potential.followPushes(unit))
  }
  protected def moveForcefully(): Boolean = {
    applySeparationForces()
    val radians         = unit.agent.forces.sum.radians
    val forceGoal       = MicroPathing.getWaypointInDirection(unit, radians).orElse(unit.agent.toTravel)
    unit.agent.toTravel = forceGoal.orElse(unit.agent.toTravel)
    if (forceGoal.isEmpty) {
      With.logger.micro(f"$unit found no waypoint towards ${Math.toDegrees(radians)} degrees.")
    }
    move()
  }

  ////////////////////
  // Decisionmaking //
  ////////////////////

  protected def readyToApproachTarget: Boolean = {
    if (target.isEmpty) return false
    val effectiveCooldown = if (unit.transport.exists(_.flying)) 0 else unit.framesToBeReadyForAttackOrder
    if (unit.unitClass.ranged && effectiveCooldown > unit.framesToGetInRange(target.get) + 4) return false
    true
  }

  protected val confidenceChaseThreshold = 0.25
  protected def getIdealTargetDistance: Double = {
    if (target.isEmpty) return 0.0
    val t = target.get
    lazy val distance           = unit.pixelDistanceEdge(t)
    lazy val range              = unit.pixelRangeAgainst(t)
    lazy val rangeAgainstUs     = if (t.canAttack(unit)) Some(t.pixelRangeAgainst(unit)) else None
    lazy val rangeEqual         = rangeAgainstUs.contains(range)
    lazy val pixelsOutranged    = rangeAgainstUs.map(_ - unit.pixelRangeAgainst(t)).filter(_ > 0)
    lazy val scourgeApproaching = unit.matchups.threats.exists(t => Zerg.Scourge(t) && t.pixelDistanceEdge(unit) < 32 * 5)
    lazy val projectedUs        = unit.pixel.projectUpTo(t.pixel, 16)
    lazy val projectedTarget    = t.pixel.projectUpTo(t.presumptiveStep, 16)
    lazy val targetApproaching  = unit.pixelDistanceSquared(t) > unit.pixelDistanceSquared(projectedTarget)
    lazy val targetEscaping     = unit.pixelDistanceSquared(t) < unit.pixelDistanceSquared(projectedTarget)
    lazy val inChoke            = ! unit.flying && unit.zone.edges.exists(_.contains(unit.pixel))
    lazy val sameThreatsChasing = unit.matchups.threats.forall(t => t.inRangeToAttack(unit) == t.inRangeToAttack(unit, projectedUs))
    lazy val obiwanned          = ! unit.flying && ! t.flying && ! unit.unitClass.melee && t.altitude > unit.altitude
    lazy val chaseDistance      = rangeAgainstUs.filter(_ < range).map(r => Math.max(24, (range - r) / 2)).getOrElse(0d)
    // Push through choke
    if ( ! unit.flying && ! t.canMove && ! t.canAttack(unit)) return ?(inChoke && nudgedTowards, 0, range)
    // Some units shouldn't even try to chase
    if (unit.isAny(Terran.SiegeTankUnsieged, Protoss.Reaver)) return range
    // If we're safe, why not chase
    if (unit.matchups.threatDeepest.isEmpty) return 0
    // Some units should usually chase flying targets
    if (unit.isAny(Terran.Wraith, Protoss.Corsair, Protoss.Scout, Zerg.Mutalisk) && t.flying && ! scourgeApproaching && ( ! Zerg.Overlord(t) || unit.matchups.pixelsEntangled < -64)) return 0
    // Chase if they outrange us
    if (pixelsOutranged.isDefined) return chaseDistance
    // Chase if we're in a choke and being nudged towards them
    if (inChoke && nudgedTowards) return chaseDistance
    // Chase if we're confident and fighting up high ground
    if (obiwanned) return chaseDistance
    // Chase if we're confident and being nudged towards them
    if (confidentEnoughToChase && nudgedTowards) return chaseDistance
    // Chase if we're confident and they're about to escape us
    if (confidentEnoughToChase && targetEscaping) return chaseDistance
    // Chase if they're escaping out of our ideal range and chasing won't bring us into range of anyone new
    if (targetEscaping && sameThreatsChasing) return chaseDistance
    // Breathe if range is equal and they shoot first
    if (rangeEqual && t.cooldownLeft < unit.cooldownLeft) return range + unit.topSpeed * Math.max(0, unit.cooldownLeft - With.latency.latencyFrames - unit.framesToTurnTo(t))
    // Maintain maximum range if chasing puts us in range of anyone new
    if ( ! sameThreatsChasing) return range
    // Maintain maximum range if they're a threat coming towards us
    if (targetApproaching) return range
    // Maintain just enough range if we're being nudged
    if (nudgedTowards) return chaseDistance
    // Otherwise, stay near max range. Get a little closer if we can in case they try to flee
    rangeAgainstUs.map(rangeAgainst => Maff.clamp(rangeAgainst + 64, range - 16, range)).getOrElse(range - 16)
  }

  //////////////////
  // Mini-actions //
  //////////////////

  protected def move(): Boolean = {
    if (unit.canMove) {
      if (Protoss.Reaver(unit) && unit.agent.ride.isDefined && ! unit.agent.commit && ! unit.loaded) {
        val destination = unit.agent.destination
        if (target.forall(unit.pixelsToGetInRange(_) > 64)) {
          BeReaver.demandPickup(unit)
        }
      }
      Commander.move(unit)
    }
    unit.unready
  }

  protected def walk(): Boolean = {
    unit.agent.shouldFight = true // This encourages the unit to use its forward formation rather than its retreat formation
    unit.agent.fightReason = "Walking"
    if (unit.intent.canSneak) {
      MicroPathing.tryMovingAlongTilePath(unit, MicroPathing.getSneakyPath(unit))
    } else if (unit.agent.receivedPushPriority() > unit.agent.priority) {
      applyForce(Forces.travel, Potential.towards(unit, unit.agent.destination))
      moveForcefully()
    } else {
      move()
    }
    unit.unready
  }

  protected def dodge(): Boolean = {
    if (unit.ready) {
      unit.agent.escalatePriority(TrafficPriorities.Shove) // Just lower than the dodge itself
      if (MicroPathing.setWaypointForcefully(unit)) {
        move()
      }
    }
    unit.unready
  }

  protected def attackIfReady(): Boolean = {
    if (unit.ready && unit.readyForAttackOrder && readyToApproachTarget) {
      Commander.attack(unit)
    }
    unit.unready
  }

  protected def aim(): Boolean = {
    if (unit.ready) {
      Target.choose(unit, TargetFilterVisibleInRange)
      Commander.attack(unit)
      Commander.hold(unit)
    }
    unit.unready
  }

  protected def potshot(): Boolean = {
    if (unit.ready && unit.readyForAttackOrder) {
      val potshotTarget = Maff.orElse(
          Maff.minBy(unit.matchups.threats)(_.pixelDistanceEdge(unit)).filter(TargetFilterPotshot.legal(unit, _)),
          target.filter(TargetFilterPotshot.legal(unit, _)),
          Target.best(unit, TargetFilterPotshot)).headOption
      unit.agent.toAttack = potshotTarget.orElse(target)
      if (potshotTarget.exists(TargetFilterPotshot.legal(unit, _))) {
        unit.agent.act("Potshot")
        Commander.attack(unit)
      }
    }
    unit.unready
  }

  protected def charge(): Boolean = {
    if ( ! target.exists(unit.inRangeToAttack) || ! attackIfReady()) {
      // If we have an attack formation
      if (engageFormation.isDefined && ! unit.flying && shouldChase && formationHelpsChase) {
        unit.agent.act("Slide")
        Commander.move(unit)
      } else if (unit.isAny(Terran.Wraith, Protoss.Corsair, Protoss.Scout, Zerg.Mutalisk, Zerg.Scourge) && target.exists(t =>
        t.flying
        && unit.pixelDistanceEdge(t)          > 0.25 * unit.pixelRangeAgainst(t)
        && unit.speedApproachingEachOther(t)  < 0
        && unit.speedApproaching(t)           < 0.9 * unit.topSpeed)) {
        chase()
      } else if (breakFormationToAttack) {
        if (shouldChase && idealDistanceForward >= 32) {
          chase()
        } else {
          unit.agent.act("Approach")
          Commander.attack(unit)
        }
      } else {
        unit.agent.act("Move")
        move()
      }
    }
    unit.unready
  }

  protected def chase(): Boolean = {
    if (unit.ready && target.isDefined) {
      unit.agent.act("Chase")
      val to                  = target.get.pixel
      val step                = target.get.presumptiveStep
      val chaseGoal           = if (step.traversableBy(unit) && unit.pixelDistanceSquared(step) >= unit.pixelDistanceSquared(to)) step else to
      val extraChaseDistance  = Math.max(0, unit.pixelDistanceCenter(chaseGoal) - unit.pixelDistanceCenter(to))
      unit.agent.toTravel     = Some(unit.pixel.project(chaseGoal, idealDistanceForward + extraChaseDistance))
      move()
    }
    unit.unready
  }

  protected def abuse(): Boolean = {
    var kite = unit.matchups.threatDeepest.exists(t => t.pixelsToGetInRange(unit) < ?(t.unitClass.isWarrior && t.presumptiveTarget.contains(unit), 80, 16) + ?(unit.canAttack(t), Math.max(0, unit.pixelRangeAgainst(t) - unit.pixelDistanceEdge(t) - 16), 48))
    kite &&= unit.confidence11 < 0.75
    kite ||= ! attackIfReady()
    if (kite) {
      Retreat(unit)
    }
    if (idealDistanceForward > 0) {
      chase()
    }
    unit.unready
  }

  protected def engage(): Boolean = {
    if (unit.ready) {
      breakFormationToAttack  = engageFormation.isEmpty || target.exists(targ =>
        // If we're not ready to attack yet, just slide into formation
        (readyToApproachTarget || unit.unitClass.melee)
        && unit.battle.isDefined
        && ! engageFormation.exists(_.flanking)
        && (
          // Break if we are already in range
          unit.inRangeToAttack(targ)
            // Break if we're just pillaging
            || unit.confidence11 > 0.75
            // Break if the fight has already begun and the formation isn't helping us
            || (squadEngaged && ! formationHelpsEngage)
            // Break if we are closer to range than the formation, and already pretty close
            || (targetDistanceHere < 32 + Math.min(targetDistanceThere, 32 * 8) && Maff.isTowards(unit.pixel, targ.pixel, unit.pixel.radiansTo(unit.agent.destination)))))

      unit.agent.toAttackFrom = Maff.orElse(SwapIf(breakFormationToAttack, engageFormation.flatMap(_(unit)).toIterable, unit.agent.toAttackFrom.toIterable)).headOption
      if (pushThrough) {
        unit.agent.act("Push")
        applySeparationForces()
        if (unit.agent.forces(Forces.spacing).lengthFast > 0 || ! attackIfReady()) {
          applyForce(Forces.travel, Potential.towards(unit, engageFormation.flatMap(_(unit)).orElse(unit.agent.toAttackFrom).getOrElse(unit.agent.destination)))
          applyForce(Forces.target, Potential.towards(unit, unit.agent.toAttackFrom.getOrElse(unit.agent.destination)))
          applyForce(Forces.threat, Potential.softAvoidThreatRange(unit) * 1.5)
          moveForcefully()
        }
      } else if (squadEngaged) {
        if (techniqueIs(Scavenge)) {
          if ( ! potshot()) {
            val framesSafe = unit.matchups.threatSoonest.map(_.framesToLaunchAttack(unit)).getOrElse(Forever())
            val framesToAttack = target.map(unit.framesToLaunchAttack).getOrElse(0)
            unit.agent.act("Snipe")
            if (framesToAttack >= framesSafe || ! attackIfReady()) {
              unit.agent.act("Lurk")
              unit.agent.escalatePriority(TrafficPriorities.Bump) // Stronger than attackers, less than full retreaters
              applyForce(Forces.travel,   Potential.towardsTarget(unit))
              applyForce(Forces.threat,   Potential.hardAvoidThreatRange(unit, Math.max(unit.topSpeed * framesToAttack, 64.0)))
              applyForce(Forces.leaving,  Potential.towards(unit, unit.agent.safety) * ground10)
              moveForcefully()
            }
          }
        } else if (readyToApproachTarget || unit.unitClass.melee || ! unit.matchups.wantsToVolley.contains(true) || shouldChase) {
          unit.agent.act("Engage")
          charge()
        } else {
          unit.agent.act("Reposition")
          val urgency01 = Math.max(0.0, (unit.cooldownMaxAirGround - unit.cooldownLeft).toDouble / (unit.cooldownMaxAirGround))
          applyForce(Forces.travel,   Potential.towardsTarget(unit) * urgency01)
          applyForce(Forces.threat,   Potential.softAvoidThreatRange(unit))
          applyForce(Forces.leaving,  Potential.towards(unit, unit.agent.safety) * ground10 * (1 - urgency01))
          moveForcefully()
        }
      } else if (assembling && formations.nonEmpty && group.groupUnits.size > 1) {
        unit.agent.act("Assemble")
        val targetRangeDelta  = unit.matchups.pixelsToTargetRange.getOrElse(unit.sightPixels.toDouble) - group.meanAttackerTargetDistance
        val forwardness       = if (unit.flying) 0.0 else Maff.fastTanh11(targetRangeDelta / 96.0)
        applyForce(Forces.travel,     Potential.towards(unit, unit.agent.destination))
        applyForce(Forces.threat,     Potential.hardAvoidThreatRange(unit, 96.0))
        applyForce(Forces.regrouping, Potential.regroup(unit))
        moveForcefully()
        restrained = target.forall(t => unit.pixelsToGetInRange(t) < unit.pixelsToGetInRangeFrom(t, unit.agent.destination))
        restrained ||= unit.intent.toTravel.exists(p => unit.pixelDistanceTravelling(p) < unit.pixelDistanceTravelling(unit.agent.destination, p))
      } else {
        unit.agent.act("Attack")
        charge()
      }
    }
    unit.unready
  }
}
