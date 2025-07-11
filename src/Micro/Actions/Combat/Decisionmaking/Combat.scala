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
import Micro.Heuristics.Potential
import Micro.Targeting.FiltersSituational.{TargetFilterPotshot, TargetFilterVisibleInRange}
import Micro.Targeting.Target
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Tactic.Squads.{GenericFriendlyUnitGroup, TFriendlyUnitGroup, UnitGroup}
import Utilities.Time.{Forever, Seconds}
import Utilities.UnitFilters.{IsSpeedling, IsSpeedlot, IsWarrior}
import Utilities.{?, SomeIf}

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
  protected def squadEngagedUpon      : Boolean = unit.squad.map(_.engagedUpon).getOrElse(unit.matchups.engagedUpon)
  protected def squadEngagingOn       : Boolean = unit.squad.map(_.engagingOn).getOrElse(unit.matchups.engagingOn)
  protected def fightConsensus        : Boolean = unit.squad.map(_.fightConsensus).getOrElse(unit.agent.shouldFight)
  protected def assembling            : Boolean = false && unit.formationEngage.isDefined && ! group.engagedUpon && Math.max(0.25, group.confidence11) < 0.95 - group.restrainedFrames / Seconds(10)()
  protected def targetFleeing         : Boolean = target.exists(t => t.canMove && Maff.isTowards(unit.pixel, t.pixel, t.angleRadians))
  protected def targetDistanceHere    : Double  = target.map(unit.pixelDistanceEdge).getOrElse(0d)
  protected def targetDistanceThere   : Double  = target.map(unit.pixelDistanceEdgeFrom(_, unit.agent.destinationNext())).getOrElse(0d)
  protected def formationHelpsEngage  : Boolean = targetDistanceThere <= Math.min(targetDistanceHere, target.map(unit.pixelRangeAgainst).getOrElse(0d))
  protected def formationHelpsChase   : Boolean = targetDistanceThere <= targetDistanceHere
  protected def idealDistanceForward  : Double  = target.map(unit.pixelDistanceEdge(_) - idealTargetDistance).getOrElse(0d)
  protected def confidentEnoughToChase: Boolean = unit.confidence11 > confidenceChaseThreshold
  protected def shouldChase           : Boolean = (idealDistanceForward > 0 && confidentEnoughToChase && targetFleeing) || (target.exists(_.isAny(Terran.SiegeTankSieged, Protoss.Reaver)) && ! unit.flying && ! Protoss.Reaver(unit))
  protected def allThreatsAbusable    : Boolean = unit.matchups.threats.forall(t => canAbuse(t) || t.pixelsToGetInRange(unit) > 24 * 5)
  protected def nudged                : Boolean = unit.agent.receivedPushPriority() > TrafficPriorities.Nudge
  protected def nudgedTowards         : Boolean = nudged && Maff.isTowards(unit.pixel, unit.agent.destinationNext(), unit.agent.receivedPushForce().radians)
  protected def pushThrough           : Boolean = shouldEngage && unit.formationEngage.isDefined && nudged && nudgedTowards && ! unit.flying
  protected def ground10              : Double  = ?(unit.flying, 0, 1)
  protected def shouldRetreat         : Boolean = techniqueIs(Fallback, Flee, Excuse)
  protected def shouldEngage          : Boolean = techniqueIs(Fight, Abuse, Scavenge)
  protected def canAbuse(t: UnitInfo) : Boolean = unit.canAttack(t) && unit.intent.canFight && ( ! t.canAttack(unit) || (unit.topSpeed > t.topSpeed && unit.pixelRangeAgainst(t) > t.pixelRangeAgainst(unit)))

  override def allowed(unit: FriendlyUnitInfo): Boolean = (unit.canMove || unit.canAttack) && unit.intent.canFight
  override def perform(unused: FriendlyUnitInfo = null): Unit = {
    restrained = false
    innerPerform()
    restrainedFrames += ?(restrained, 1.0, -0.5) * With.framesSince(unit.agent.lastFrame)
    restrainedFrames = Math.max(0, restrainedFrames)
  }
  def innerPerform(): Unit = {
    if (unit.agent.toAttack.isEmpty) {
      Target.choose(unit)
    }
    unit.agent.choosePerch()
    Commander.defaultEscalation(unit)

    framesUntilShot       = Maff.min((Seq(unit.pixel) ++ unit.agent.perch.pixel).view.flatMap(p =>  unit.matchups.threats.map(_.framesToGetInRange(unit, p)))).getOrElse(Forever())
    framesToPokeTarget    = target.map(unit.framesToLaunchAttack(_) + unit.unitClass.framesToPotshot + With.reaction.agencyMax + With.latency.latencyFrames)
    idealTargetDistance   = getIdealTargetDistance
    hasSpacetimeToPoke    = framesToPokeTarget.exists(_ < framesUntilShot) && unit.agent.perch.pixel.forall(p => ! unit.matchups.threats.exists(t => t.inRangeToAttack(unit, p.project(t.pixel, 16))))
    lazy val safePassage  = unit.matchups.ignorant || unit.topSpeedTransported >= 0.9 * group.meanTopSpeed && unit.matchups.pixelsToThreatRange.forall(_ > 32 * ?(unit.agent.receivedPushPriority() < TrafficPriorities.Bump, 2, 16))

    technique =
            if ( ! unit.canMove)                                    Fight
      else  if (unit.agent.shouldFight && target.isDefined)         Fight
      else  if (safePassage && target.exists(unit.inRangeToAttack)) Fight
      else  if (safePassage)                                        Walk
      else                                                          Flee

    transition(Aim,       ! unit.canMove)
    transition(Dodge,     unit.agent.receivedPushPriority() >= TrafficPriorities.Dodge)
    transition(Abuse,     unit.unitClass.abuseAllowed && unit.agent.receivedPushPriority() == TrafficPriorities.Freedom && unit.matchups.targetNearest.exists(canAbuse) && (hasSpacetimeToPoke || allThreatsAbusable) && (shouldRetreat || framesUntilShot < unit.cooldownMaxAirGround))
    transition(SuperKite, unit.isAny(Terran.Wraith, Zerg.Mutalisk))
    transition(Scavenge,
      target.exists(_.matchups.framesToLive > unit.matchups.framesToLive)
      && unit.matchups.threatDeepest.exists(t => unit.canAttack(t) && unit.pixelRangeAgainst(t) >= t.pixelRangeAgainst(unit))
      && unit.totalHealth * 2.0 <= group.meanAttackerHealth
      && unit.totalHealth * 3.0 <= unit.matchups.threatsInPixels(96).map(_.damageOnNextHitAgainst(unit)).sum)
    transition(Fallback,  unit.isAny(Terran.SiegeTankUnsieged, Terran.Goliath, Protoss.Reaver) && ! unit.airborne)
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
      unit.agent.decision.set(unit.agent.safety)
      if (unit.agent.shouldFight) {
        unit.agent.shouldFight = false
        unit.agent.fightReason = "Technique"
      }
    }

    if (technique == Walk       && walk())        return
    if (technique == Aim        && aim())         return
    if (technique == Dodge      && dodge())       return
    if (technique == Fallback   && potshot())     return
    if (shouldRetreat           && Retreat(unit)) return
    if (shouldEngage            && Brawl(unit))   return
    if (technique == Abuse      && abuse())       return
    if (technique == SuperKite  && superkite())   return
    if (shouldEngage            && engage())      {}
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
    val radians   = unit.agent.forces.sum.radians
    val forceGoal = MicroPathing.getWaypointInDirection(unit, radians).orElse(unit.agent.destinationNext.pixel)
    unit.agent.forced.set(forceGoal)
    if (forceGoal.isEmpty) { With.logger.micro(f"$unit found no waypoint towards ${Math.toDegrees(radians)} degrees.") }
    move()
  }

  ////////////////////
  // Decisionmaking //
  ////////////////////

  protected def readyToApproachTarget: Boolean = {
    if (target.isEmpty) return false
    if (unit.unitClass.melee) return true
    if (?(unit.airlifted, 0, unit.framesToBeReadyForAttackOrder) > unit.framesToGetInRange(target.get) + 4) return false
    true
  }

  protected val confidenceChaseThreshold = 0.25
  protected def getIdealTargetDistance: Double = {
    if (target.isEmpty) return 0.0
    val t = target.get
    lazy val distance           = unit.pixelDistanceEdge(t)
    lazy val range              = unit.pixelRangeAgainst(t)
    lazy val rangeAgainstUs     = SomeIf(t.canAttack(unit), t.pixelRangeAgainst(unit))
    lazy val rangeEqual         = rangeAgainstUs.contains(range)
    lazy val pixelsOutranged    = rangeAgainstUs.map(_ - unit.pixelRangeAgainst(t)).filter(_ > 0)
    lazy val scourgeApproaching = unit.matchups.threats.exists(t => Zerg.Scourge(t) && t.pixelDistanceEdge(unit) < 32 * 5)
    lazy val projectedUs        = unit.pixel.projectUpTo(t.pixel, 16)
    lazy val projectedTarget    = t.pixel.projectUpTo(t.presumptiveDestinationNext, 16)
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
        if (target
          .map(t => Math.min(unit.pixelsToGetInRange(t), unit.pixelsToGetInRange(t, t.projectFrames(48))) > 64)
          .getOrElse(unit.pixelDistanceTravelling(unit.agent.destinationNext()) > 96)) {
          BeReaver.clickShuttle(unit)
        }
      }
      Commander.move(unit)
    }
    unit.unready
  }

  protected def walk(): Boolean = {
    unit.agent.shouldFight = true // This encourages the unit to use its forward formation rather than its retreat formation
    unit.agent.fightReason = "Walking"

    unit.agent.perch.clear() // We're not picking a fight just yet
    if (unit.intent.canSneak) {
      MicroPathing.tryMovingAlongTilePath(unit, MicroPathing.getSneakyPath(unit))
    } else if (unit.agent.receivedPushPriority() > unit.agent.priority) {
      applyForce(Forces.travel, Potential.towards(unit, unit.agent.destinationNext()))
      moveForcefully()
    } else {
      move()
    }
    unit.unready
  }

  protected def dodge(): Boolean = {
    if (unit.ready) {
      unit.agent.escalatePriority(TrafficPriorities.Shove) // Just lower than the dodge itself
      unit.agent.forces(Forces.threat) = Potential.followPushes(unit)
      MicroPathing.moveForcefully(unit)
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
      val potshotTarget = Maff.orElseFiltered(
          Maff.minBy(unit.matchups.threats)(_.pixelDistanceEdge(unit)).filter(TargetFilterPotshot.legal(unit, _)),
          target.filter(TargetFilterPotshot.legal(unit, _)),
          Target.best(unit, TargetFilterPotshot))(unit.canAttack).headOption
      unit.agent.toAttack = potshotTarget.orElse(target)
      if (potshotTarget.exists(TargetFilterPotshot.legal(unit, _))) {
        unit.agent.act("Potshot")
        Commander.attack(unit)
      }
    }
    unit.unready
  }

  protected def charge(): Boolean = {
    if (unit.ready && ( ! target.exists(unit.inRangeToAttack) || ! attackIfReady())) {
      // If we have an attack formation
      if (unit.formationEngage.isDefined && ! unit.flying && shouldChase && formationHelpsChase) {
        unit.agent.act("Slide")
        unit.agent.escalatePriority(TrafficPriorities.Nudge)
        move()

      } else if (unit.isAny(Terran.Wraith, Protoss.Corsair, Protoss.Scout, Zerg.Mutalisk, Zerg.Scourge) && target.exists(t =>
        t.flying
        && unit.pixelDistanceEdge(t)          > 0.25 * unit.pixelRangeAgainst(t)
        && unit.speedApproachingEachOther(t)  < 0
        && unit.speedApproaching(t)           < 0.9 * unit.topSpeed)) {
        unit.agent.act("Dogfight")
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
      val step                = target.get.presumptiveDestinationNext
      val chaseGoal           = ?(step.traversableBy(unit) && unit.pixelDistanceSquared(step) >= unit.pixelDistanceSquared(to), step, to)
      val extraChaseDistance  = Math.max(0, unit.pixelDistanceCenter(chaseGoal) - unit.pixelDistanceCenter(to))
      unit.agent.decision.set(unit.pixel.project(chaseGoal, idealDistanceForward + extraChaseDistance))
      unit.agent.escalatePriority(TrafficPriorities.Nudge)
      move()
    }
    unit.unready
  }

  protected def abuse(): Boolean = {
    var minimumSpace = 16.0
    unit.matchups.threatDeepest.foreach(t => {
      if (IsWarrior(t)) {
        minimumSpace += 32
        if (t.presumptiveTarget.contains(unit)) {
          minimumSpace += 32
        }
        if (t.topSpeed > unit.topSpeed) {
          minimumSpace += 32
        }
      }
      lazy val rangeDifferential = unit.pixelRangeAgainst(t) - t.pixelRangeAgainst(unit)
      if (unit.canAttack(t) && rangeDifferential > 0) {
        minimumSpace = Math.min(minimumSpace, rangeDifferential / 2)
      }
    })
    if (unit.readyForAttackOrder) {
      minimumSpace = Math.min(minimumSpace, target.map(unit.pixelRangeAgainst).getOrElse(unit.pixelRangeMax) - 24)
    }

    if (unit.matchups.threatDeepest.exists(_.pixelsToGetInRange(unit) < minimumSpace)) {
      Retreat(unit)
    }
    if ( ! attackIfReady() && unit.matchups.threatDeepest.exists(a => a.presumptiveTarget.contains(unit) || a.speedApproaching(unit) > 0)) {
      Retreat(unit)
    }
    if (idealDistanceForward > 0) {
      chase()
    }
    charge()
    unit.unready
  }

  protected def superkite(): Boolean = {
    target.foreach(t => {
      var kite  = false
      var chase = false
      if (unit.readyForAttackOrder) {
        if (unit.framesToGetInRange(t) < With.game.getRemainingLatencyFrames) {
          if (unit.orderTarget.contains(t)) {
            kite = true
          } else {
            Commander.attack(unit)
          }
        } else {
          chase = true
        }
      } else if (unit.matchups.framesOfSafety < unit.cooldownLeft + unit.cooldownMaxAgainst(t)) {
        kite = true
      }
      if (kite) {
        unit.agent.forces(Forces.threat)      = Potential.hardAvoidThreatRange(unit)
        unit.agent.forces(Forces.regrouping)  = Potential.regroup(unit)
        MicroPathing.moveForcefully(unit)
      } else if (chase) {
        unit.agent.forces(Forces.target)      = Potential.towards(unit, t.pixel)
        unit.agent.forces(Forces.regrouping)  = Potential.regroup(unit) * 0.5
        MicroPathing.moveForcefully(unit)
      }
    })

    unit.unready
  }

  protected def engage(): Boolean = {
    if (unit.ready) {
      breakFormationToAttack  = unit.formationEngage.isEmpty
      breakFormationToAttack ||= target.exists(targ =>
        // Maintain formation if we're not ready to attack yet
        readyToApproachTarget
        // Maintain formation until arriving at a fight or the destinatation
        && (unit.battle.isDefined || unit.pixelDistanceEdge(targ) <= unit.sightPixels || (unit.pixelDistanceEdge(targ) <= 256 && targ.visible))
        // Maintain formation while flanking
        && ! unit.formationEngage.exists(_.flanking)
        // Maintain formation until battle breaks out
        && ( ! formationHelpsEngage || squadEngagedUpon))
      breakFormationToAttack ||= target.exists(unit.pixelsToGetInRange(_) < Math.max(0, 128 * unit.confidence11))
      breakFormationToAttack ||= With.yolo.active

      if ( ! breakFormationToAttack) {
        unit.agent.perch.clear()
      }
      if (pushThrough) {
        unit.agent.act("Push")
        applySeparationForces()
        if (unit.agent.forces(Forces.spacing).lengthFast > 0 || ! attackIfReady()) {
          applyForce(Forces.travel, Potential.towards(unit, unit.agent.station.pixel.orElse(unit.agent.perch.pixel).getOrElse(unit.agent.destinationNext())))
          applyForce(Forces.target, Potential.towards(unit,                                 unit.agent.perch.pixel .getOrElse(unit.agent.destinationNext())))
          applyForce(Forces.threat, Potential.softAvoidThreatRange(unit) * 1.5)
          moveForcefully()
        }
      } else if (assembling && group.groupUnits.size > 1) {
        unit.agent.act("Assemble")
        val targetRangeDelta  = unit.matchups.pixelsToTargetRange.getOrElse(unit.sightPixels.toDouble) - group.meanAttackerTargetDistance
        val forwardness       = if (unit.flying) 0.0 else Maff.fastTanh11(targetRangeDelta / 96.0)
        applyForce(Forces.travel,     Potential.towards(unit, unit.agent.destinationNext()))
        applyForce(Forces.threat,     Potential.hardAvoidThreatRange(unit, 96.0))
        applyForce(Forces.regrouping, Potential.regroup(unit))
        moveForcefully()
        restrained = target.forall(t => unit.pixelsToGetInRange(t) < unit.pixelsToGetInRangeFrom(t, unit.agent.destinationNext()))
        restrained ||= unit.intent.terminus.exists(p => unit.pixelDistanceTravelling(p) < unit.pixelDistanceTravelling(unit.agent.destinationNext(), p))
      } else if (techniqueIs(Scavenge)) {
        if ( ! potshot()) {
          unit.agent.act("Snipe")
          val framesSafe      = unit.matchups.threatSoonest.map(_.framesToLaunchAttack(unit)).getOrElse(Forever())
          val framesToAttack  = target.map(unit.framesToLaunchAttack).getOrElse(0)
          if (framesToAttack >= framesSafe || ! attackIfReady()) {
            unit.agent.act("Lurk")
            unit.agent.escalatePriority(TrafficPriorities.Bump) // Stronger than attackers, less than full retreaters
            applyForce(Forces.target,   Potential.towardsTarget(unit))
            applyForce(Forces.threat,   Potential.hardAvoidThreatRange(unit, Math.max(unit.topSpeed * framesToAttack, 64.0)))
            applyForce(Forces.leaving,  Potential.towards(unit, unit.agent.safety) * ground10)
            moveForcefully()
          }
        }
      } else if (readyToApproachTarget || ! unit.matchups.wantsToVolley.contains(true) || shouldChase) {
        unit.agent.act("Engage")
        charge()
      } else {
        unit.agent.act("Reposition")
        val urgency01 = Math.max(0.0, (unit.cooldownMaxAirGround - unit.cooldownLeft).toDouble / (unit.cooldownMaxAirGround))
        // Haven't tried this out; might help with sliding into formation
        // engageFormation.flatMap(_(unit)).foreach(f => applyForce(Forces.travel, Potential.towards(unit, f)))
        applyForce(Forces.target,   Potential.towardsTarget(unit) * urgency01)
        applyForce(Forces.threat,   Potential.softAvoidThreatRange(unit))
        applyForce(Forces.leaving,  Potential.towards(unit, unit.agent.safety) * ground10 * (1 - urgency01))
        moveForcefully()
      }
    }
    unit.unready
  }
}
