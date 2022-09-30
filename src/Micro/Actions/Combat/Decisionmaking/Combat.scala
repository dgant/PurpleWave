package Micro.Actions.Combat.Decisionmaking

import Debugging.Visualizations.{ForceLabel, Forces}
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Physics.Force
import Mathematics.Points.Pixel
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
import Utilities.?
import Utilities.UnitFilters.{IsSpeedling, IsSpeedlot, IsWorker}

final class Combat(unit: FriendlyUnitInfo) extends Action {

  var bloodlustFrames : Double            = 0.0
  var restraining     : Boolean           = false
  var target          : Option[UnitInfo]  = None
  var technique       : Technique         = Fight

  protected def group             : UnitGroup with TFriendlyUnitGroup = unit.squad.orElse(unit.friendlyTeam).getOrElse(new GenericFriendlyUnitGroup(unit))
  protected def formations        : Seq[Formation] = unit.squad.map(_.formations).getOrElse(Seq.empty)
  protected def engageFormation   : Option[Formation] = formations.dropRight(1).headOption
  protected def retreatFormation  : Option[Formation] = formations.lastOption
  protected def squadEngaged      : Boolean = unit.squad.map(_.engagedUpon).getOrElse(unit.matchups.engagedUpon)
  protected def fightConsensus    : Boolean = unit.squad.map(_.fightConsensus).getOrElse(unit.agent.shouldFight)
  protected def targetFleeing     : Boolean = target.exists(t => t.canMove && Maff.isTowards(unit.pixel, t.pixel, t.angleRadians))
  protected def chase             : Boolean = idealDistanceForward > 0 && confidentEnoughToChase && targetFleeing
  protected def allThreatsAbusable: Boolean = unit.matchups.threats.forall(canAbuse)
  protected def nudged            : Boolean = unit.agent.receivedPushPriority() > TrafficPriorities.Nudge
  protected def nudgedTowards     : Boolean = nudged && Maff.isTowards(unit.pixel, unit.agent.destination, unit.agent.receivedPushForce().radians)
  protected def pushThrough       : Boolean = shouldEngage && engageFormation.isDefined && nudged && nudgedTowards && ! trapped

  protected def canAbuse(t: UnitInfo): Boolean = unit.canAttack(t) && ( ! t.canAttack(unit) || (unit.topSpeed > t.topSpeed && unit.pixelRangeAgainst(t) > t.pixelRangeAgainst(unit)))

  var firingPixel           : Option[Pixel] = None
  var framesToPokeTarget    : Option[Int] = None
  var targetDistanceHere    : Double = _
  var targetDistanceThere   : Double = _
  var idealTargetDistance   : Double = _
  var idealDistanceForward  : Double = _
  var shouldEngage          : Boolean = _
  var shouldRetreat         : Boolean = _
  var hasSpacetimeToPoke    : Boolean = _
  var formationHelpsEngage  : Boolean = _
  var formationHelpsChase   : Boolean = _
  var confidentEnoughToChase: Boolean = _
  var breakFormationToAttack: Boolean = _
  var timeToReposition      : Boolean = _
  var assembling            : Boolean = _
  var trapped               : Boolean = _

  override def allowed(unit: FriendlyUnitInfo): Boolean = (unit.canMove || unit.canAttack) && unit.intent.canFight

  override def perform(unused: FriendlyUnitInfo): Unit = {
    act()

    // Set traffic priority
    if (target.exists( ! unit.inRangeToAttack(_))) {
      unit.agent.escalatePriority(TrafficPriorities.Nudge)
    }
    if ( ! unit.agent.shouldFight) {
      if (unit.matchups.pixelsEntangled > -96) {
        unit.agent.escalatePriority(TrafficPriorities.Shove)
      } else if (unit.matchups.pixelsEntangled > -160) {
        unit.agent.escalatePriority(TrafficPriorities.Bump)
      }
    }
  }
  private def act(): Unit = {

    target = Target.choose(unit)
    unit.agent.shouldFight &&= target.nonEmpty || unit.matchups.threats.forall(IsWorker) || unit.matchups.targets.nonEmpty

    //////////////////////
    // Choose technique //
    //////////////////////

    technique =
         if ( ! unit.canMove)                               Fight
    else if (unit.matchups.ignorant && fightConsensus)      Fight
    else if (unit.matchups.ignorant && formations.nonEmpty) Flee
    else if (unit.matchups.ignorant)                        Walk
    else if (unit.agent.shouldFight)                        Fight
    else                                                    Flee

    trapped               = ! unit.flying && unit.tile.adjacent9.exists(t => ! t.valid || t.units.exists(e => e.isEnemy && ! e.flying && e.canAttack(unit) && e.pixelDistanceTravelling(unit.agent.home) > unit.pixelDistanceTravelling(unit.agent.home)))
    firingPixel           = target.map(unit.pixelToFireAt)
    framesToPokeTarget    = target.map(unit.framesToGetInRange(_) + unit.unitClass.framesToPotshot + With.reaction.agencyMax + With.latency.latencyFrames)
    idealTargetDistance   = getIdealTargetDistance
    idealDistanceForward  = target.map(unit.pixelDistanceEdge(_) - idealTargetDistance).getOrElse(0.0)
    hasSpacetimeToPoke    = framesToPokeTarget.exists(framesToPoke => unit.matchups.threats.forall(threat =>
      framesToPoke < Math.min(threat.framesToGetInRange(unit), threat.framesToGetInRange(unit, firingPixel.get))
        && ! threat.inRangeToAttack(unit, firingPixel.get.project(threat.pixel, 16))))

    transition(Aim,       () => ! unit.canMove)
    transition(Dodge,     () => unit.agent.receivedPushPriority() >= TrafficPriorities.Dodge)
    transition(Abuse,     () => unit.unitClass.abuseAllowed && unit.matchups.targetNearest.exists(canAbuse) && (hasSpacetimeToPoke || allThreatsAbusable))
    transition(Scavenge,  () =>
         unit.matchups.threatDeepest.exists(t => unit.canAttack(t) && unit.pixelRangeAgainst(t) >= t.pixelRangeAgainst(unit))
      && unit.totalHealth * 2.0 <= group.meanAttackerHealth
      && unit.totalHealth * 2.0 <= unit.matchups.threatsInPixels(64).map(_.damageOnNextHitAgainst(unit)).sum)
    transition(Fallback,  () => unit.isAny(Terran.SiegeTankUnsieged, Terran.Goliath, Protoss.Reaver))
    transition(Fallback,  () => Protoss.Dragoon(unit)
      && unit.agent.receivedPushPriority() < TrafficPriorities.Shove
      && unit.matchups.targetNearest.exists(unit.inRangeToAttack)
      && unit.matchups.threatsInFrames(unit.unitClass.framesToPotshot + 9).forall(_.isAny(Terran.Vulture, IsSpeedlot, Zerg.Zergling)))
    transition(Fallback,  () => unit.isAny(Protoss.Archon, Protoss.Zealot) && unit.matchups.targetsInRange.exists(IsSpeedling))
    transition(Fallback,  () => trapped)

    if (unit.unready) return

    ///////////////
    // Set goals //
    ///////////////

    shouldRetreat = techniqueIs(Fallback, Flee, Excuse)
    shouldEngage  = techniqueIs(Fight, Abuse, Scavenge)

    unit.agent.shouldFight ||= shouldEngage
    if (shouldRetreat) unit.agent.toTravel = Some(unit.agent.safety)

    /////////////
    // Execute //
    /////////////

    if (technique == Aim && aim())          return
    if (technique == Dodge && dodge())      return
    if (technique == Walk)                  return
    if (technique == Fallback && potshot()) return
    if (shouldRetreat && Retreat(unit))     return
    if (shouldEngage  && Brawl(unit))       return
    if (technique == Abuse) {
      if (idealDistanceForward < 0 || ! attackIfReady()) {
        applyForce(Forces.threat,   Potential.hardAvoidThreatRange(unit, 64.0))
        applyForce(Forces.spacing,  Potential.avoidCollision(unit)) // Regroup if we have enough distance?
        applyForce(Forces.travel,   Potential.towardsTarget(unit))
        moveForcefully()
      }
    } else if (shouldEngage) {
      targetDistanceHere      = target.map(unit.pixelDistanceEdge).getOrElse(0d)
      targetDistanceThere     = target.map(unit.pixelDistanceEdgeFrom(_, unit.agent.destination)).getOrElse(0d)
      formationHelpsEngage    = targetDistanceThere <= Math.min(targetDistanceHere, target.map(unit.pixelRangeAgainst).getOrElse(0d))
      formationHelpsChase     = targetDistanceThere <= targetDistanceHere
      confidentEnoughToChase  = unit.confidence11 > confidenceChaseThreshold
      breakFormationToAttack  = formations.isEmpty || target.exists(targ =>
        // If we're not ready to attack yet, just slide into formation
        (readyToApproachTarget || unit.unitClass.melee) && (
          // Break if we are already in range
          unit.inRangeToAttack(targ)
            // Break if we're just pillaging
            || confidentEnoughToChase
            || unit.matchups.threats.forall(IsWorker)
            // Break if the fight has already begun and the formation isn't helping us
            || (squadEngaged && ! formationHelpsEngage && ! unit.transport.exists(_.loaded))
            // Break if we are closer to range than the formation, and already pretty close
            || (targetDistanceHere < Math.min(targetDistanceThere, 32 * 8) && Maff.isTowards(unit.pixel, target.get.pixel, unit.pixel.radiansTo(unit.agent.destination)))))

      assembling = engageFormation.isDefined
      if (pushThrough) {
        unit.agent.act("Push")
        // Move towards destination and target; away from allies, centroid, threat
        applyForce(Forces.travel,     Potential.towards(unit, engageFormation.flatMap(_(unit)).orElse(firingPixel).getOrElse(unit.agent.destination)))
        applyForce(Forces.target,     Potential.towards(unit, firingPixel.getOrElse(unit.agent.destination)))
        applyForce(Forces.spacing,    Potential.avoidCollision(unit)) // Regroup if we have enough distance?
        applyForce(Forces.spreading,  Potential.towards(unit, unit.squad.get.centroidGround) * -1)
        applyForce(Forces.threat,     Potential.softAvoidThreatRange(unit))
        if (unit.agent.forces(Forces.spacing).lengthFast > 0 || ! attackIfReady()) moveForcefully()
      } else if (squadEngaged) {
        if (techniqueIs(Scavenge)) {
          val framesFree = unit.matchups.threatSoonest.get.framesToLaunchAttack(unit)
          val framesToAttack = unit.framesToLaunchAttack(target.get)
          unit.agent.act("Snipe")
          if (framesToAttack >= framesFree || ! attackIfReady()) {
            unit.agent.act("Lurk")
            applyForce(Forces.threat,   Potential.hardAvoidThreatRange(unit, 64.0))
            applyForce(Forces.spacing,  Potential.avoidCollision(unit)) // Regroup if we have enough distance?
            applyForce(Forces.travel,   Potential.towardsTarget(unit))
            applyForce(Forces.leaving,  Potential.towards(unit, unit.agent.home))
            moveForcefully()
          }
        } else if (readyToApproachTarget || unit.unitClass.melee || ! unit.matchups.wantsToVolley.contains(true) || chase) {
          unit.agent.act("Engage")
          charge()
        } else {
          unit.agent.act("Reposition")
          val urgency = Math.max(0.0, (unit.cooldownMaxAirGround - unit.cooldownLeft) / (unit.cooldownMaxAirGround))
          applyForce(Forces.threat,   Potential.hardAvoidThreatRange(unit, 32.0))
          applyForce(Forces.spacing,  Potential.avoidCollision(unit)) // Regroup if we have enough distance?
          applyForce(Forces.travel,   Potential.towardsTarget(unit) * urgency)
          applyForce(Forces.leaving,  Potential.towards(unit, unit.agent.home) * (1 - urgency))
          moveForcefully()
        }
      } else if (assembling && formations.nonEmpty && group.groupUnits.size > 1) {
        unit.agent.act("Assemble")
        val targetRangeDelta  = unit.matchups.pixelsToTargetRange.get - unit.squad.get.meanAttackerTargetDistance
        val forwardness       = if (unit.flying) 0.0 else Maff.fastTanh11(targetRangeDelta / 96.0)
        applyForce(Forces.threat,     Potential.hardAvoidThreatRange(unit, 96.0))
        applyForce(Forces.spacing,    Potential.avoidCollision(unit)) // Regroup if we have enough distance?
        applyForce(Forces.travel,     Potential.towards(unit, unit.agent.destination))
        applyForce(Forces.spreading,  Potential.towards(unit, unit.squad.get.centroidGround)  * - forwardness)
        moveForcefully()
      } else {
        unit.agent.act("Attack")
        charge()
      }
    }
  }

  ////////////////
  // Techniques //
  ////////////////

  def techniqueIs(techniques: Technique*): Boolean = techniques.contains(technique)

  protected def transition(newTechnique: Technique, predicate: () => Boolean = () => true): Unit = {
    if (unit.ready && technique.canTransition(newTechnique) && predicate()) {
      technique = newTechnique
      unit.agent.act(technique.toString)
    }
  }

  ///////////////
  // Potential //
  ///////////////

  protected def applyForce(label: ForceLabel, force: Force): Unit = {
    unit.agent.forces.put(label, force)
  }
  protected def moveForcefully(): Unit = {
    val radians = unit.agent.forces.sum.radians
    val forceGoal = MicroPathing.getWaypointInDirection(unit, radians).orElse(unit.agent.toTravel)
    if (forceGoal.isEmpty) {
      With.logger.micro(f"$unit found no waypoint towards ${Math.toDegrees(radians)} degrees.")
    }
    unit.agent.toTravel = forceGoal.orElse(unit.agent.toTravel)
    Commander.move(unit)
  }

  ////////////////////
  // Decisionmaking //
  ////////////////////

  def readyToApproachTarget: Boolean = {
    if (unit.agent.toAttack.isEmpty) return false
    val effectiveCooldown = if (unit.transport.exists(_.flying)) 0 else unit.cooldownLeft
    if (unit.unitClass.ranged && effectiveCooldown > unit.framesToGetInRange(unit.agent.toAttack.get) + 4) return false
    true
  }

  val confidenceChaseThreshold = 0.25
  def getIdealTargetDistance: Double = {
    if (target.isEmpty) return 0.0
    val t = target.get
    lazy val distance           = unit.pixelDistanceEdge(t)
    lazy val range              = unit.pixelRangeAgainst(t)
    lazy val rangeAgainstUs     = if (t.canAttack(unit)) Some(t.pixelRangeAgainst(unit)) else None
    lazy val rangeEqual         = rangeAgainstUs.contains(range)
    lazy val pixelsOutranged    = rangeAgainstUs.map(_ - unit.pixelRangeAgainst(t)).filter(_ > 0)
    lazy val confidentToChase   = unit.confidence11 > confidenceChaseThreshold
    lazy val scourgeApproaching = unit.matchups.threats.exists(t => Zerg.Scourge(t) && t.pixelDistanceEdge(unit) < 32 * 5)
    lazy val projectedUs        = unit.pixel.projectUpTo(t.pixel, 16)
    lazy val projectedTarget    = t.pixel.projectUpTo(t.presumptiveStep, 16)
    lazy val targetApproaching  = unit.pixelDistanceSquared(t) > unit.pixelDistanceSquared(projectedTarget)
    lazy val targetEscaping     = unit.pixelDistanceSquared(t) < unit.pixelDistanceSquared(projectedTarget)
    lazy val inChoke            = ! unit.flying && unit.zone.edges.exists(_.contains(unit.pixel))
    lazy val sameThreatsChasing = unit.matchups.threats.forall(t => t.inRangeToAttack(unit) == t.inRangeToAttack(unit, projectedUs))
    lazy val obiwanned          = ! unit.flying && ! t.flying && ! unit.unitClass.melee && t.altitude > unit.altitude
    lazy val chaseDistance      = rangeAgainstUs.filter(_ < range).map(r => Math.max(24, (range - r) / 2)).getOrElse(0d)

    if ( ! t.canMove && ! t.canAttack(unit)) return ?(inChoke && nudgedTowards, 0, range)

    // Chasing behaviors
    // Some units shouldn't even try
    if (unit.isAny(Terran.SiegeTankUnsieged, Protoss.Reaver)) return range

    // Some units should usually chase flying targets
    if (unit.isAny(Terran.Wraith, Protoss.Corsair, Protoss.Scout, Zerg.Mutalisk) && t.flying && ! scourgeApproaching) return 0

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

  def move(): Boolean = {
    if ( ! unit.canMove) return false
    if (Protoss.Reaver(unit) && unit.agent.ride.isDefined && ! unit.agent.commit && ! unit.loaded) {
      val destination = unit.agent.destination
      if (unit.agent.toAttack.forall(unit.pixelsToGetInRange(_) > 64)) {
        BeReaver.demandPickup(unit)
      }
    }
    Commander.move(unit)
    unit.unready
  }

  def dodge(): Boolean = {
    if (unit.ready) {
      unit.agent.escalatePriority(TrafficPriorities.Shove) // Just lower than the dodge itself
      val towards = MicroPathing.getPushRadians(unit).flatMap(MicroPathing.getWaypointInDirection(unit, _))
      if (towards.isDefined) {
        unit.agent.toTravel = towards
        move()
      }
    }
    unit.unready
  }

  def attackIfReady(): Boolean = {
    if (unit.ready && unit.readyForAttackOrder && readyToApproachTarget) {
      Commander.attack(unit)
    }
    unit.unready
  }

  def aim(): Boolean = {
    if (unit.ready) {
      Target.choose(unit, TargetFilterVisibleInRange)
      Commander.attack(unit)
      Commander.hold(unit)
    }
    unit.unready
  }

  def potshot(): Boolean = {
    if (unit.ready && unit.readyForAttackOrder) {
      val potshotTarget = Maff.orElse(
          Maff.minBy(unit.matchups.threats)(_.pixelDistanceEdge(unit)).filter(TargetFilterPotshot.legal(unit, _)),
          unit.agent.toAttack.filter(TargetFilterPotshot.legal(unit, _)),
          Target.best(unit, TargetFilterPotshot)).headOption
      unit.agent.toAttack = potshotTarget.orElse(unit.agent.toAttack)
      if (potshotTarget.exists(TargetFilterPotshot.legal(unit, _))) {
        unit.agent.act("Potshot")
        Commander.attack(unit)
      }
    }
    unit.unready
  }

  def charge(): Boolean = {
    if ( ! target.exists(unit.inRangeToAttack) || ! attackIfReady()) {
      // If we have an attack formation
      if (formations.size > 1 && ! unit.flying && confidentEnoughToChase && formationHelpsChase && targetFleeing) {
        unit.agent.act("Slide")
        Commander.move(unit)
      } else if (breakFormationToAttack) {
        if (idealDistanceForward >= 32) {
          chase(idealDistanceForward)
        } else {
          unit.agent.act("Approach")
          Commander.attack(unit)
        }
      } else {
        move()
      }
    }
    unit.unready
  }

  def chase(distanceTowards: Double): Boolean = {
    if (unit.ready && unit.agent.toAttack.isDefined) {
      unit.agent.act("Chase")
      val to                  = target.get.pixel
      val step                = target.get.presumptiveStep
      val chaseGoal           = if (step.traversableBy(unit) && unit.pixelDistanceSquared(step) >= unit.pixelDistanceSquared(to)) step else to
      val extraChaseDistance  = Math.max(0, unit.pixelDistanceCenter(chaseGoal) - unit.pixelDistanceCenter(to))
      unit.agent.toTravel     = Some(unit.pixel.project(chaseGoal, distanceTowards + extraChaseDistance))
      move()
    }
    unit.unready
  }
}
