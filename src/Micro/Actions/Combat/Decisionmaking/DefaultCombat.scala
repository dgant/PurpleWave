package Micro.Actions.Combat.Decisionmaking

import Debugging.Visualizations.Forces
import Lifecycle.With
import Mathematics.Physics.{Force, ForceMath}
import Mathematics.PurpleMath
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

  class MicroContext(val unit: FriendlyUnitInfo, val shouldEngage: Boolean) {
    def retarget(filters: TargetFilter*): Unit = {
      // TODO: Evaluate all targets once and cache values,
      // then pick best target given custom filters
      unit.agent.toAttack = EvaluateTargets.best(unit, EvaluateTargets.preferredTargets(unit, filters: _*))
    }
    private def target: Option[UnitInfo] = unit.agent.toAttack

    lazy val receivedPushes = With.coordinator.pushes.get(unit).map(p => (p, p.force(unit))).sortBy(-_._1.priority.value)
    lazy val receivedPushForces = receivedPushes.view.filter(_._2.nonEmpty).map(p => (p._1, p._2.get))
    lazy val receivedPushPriority: TrafficPriority = ByOption.max(receivedPushes.view.map(_._1.priority)).getOrElse(TrafficPriorities.None)

    // Decide how far from target/threat we want to be
    lazy val idealPixelsFromTargetRange = if (target.exists(_.speedApproaching(unit) < 0)) -16d else 0d
    lazy val idealPixelsFromThreatRange = 64 + Math.max(0, unit.effectiveRangePixels - ByOption.min(unit.matchups.allies.view.map(_.effectiveRangePixels)).getOrElse(unit.effectiveRangePixels)) / 4 // Induce sorting

    // Decide how far from adjacent teammates we want to be
    lazy val idealPixelsFromTeammatesCollision = if (unit.flying) 0 else unit.unitClass.dimensionMax / 2
    lazy val idealPixelsFromTeammatesSplash = ByOption.max(unit.matchups.threats.view.map(_.unitClass).filter(_.dealsRadialSplashDamage).map(t => 1 + (if (unit.flying) t.airSplashRadius25 else t.groundSplashRadius25))).getOrElse(0)
    lazy val idealPixelsFromTeammates = Math.max(idealPixelsFromTeammatesCollision, idealPixelsFromTeammatesSplash)

    // Check how far from target/threat/teammate we are
    lazy val currentPixelsFromThreatRange = ByOption.min(unit.matchups.threats.view.map(_.pixelsToGetInRange(unit))).getOrElse(LightYear().toDouble)
    lazy val currentPixelsFromTargetRange = target.map(unit.pixelsToGetInRange).getOrElse(unit.pixelDistanceTravelling(unit.agent.destination))
    lazy val currentPixelsFromTeammate = unit.matchups.allies.view.map(_.pixelDistanceEdge(unit))

    lazy val missingDistanceFromThreat = idealPixelsFromThreatRange - currentPixelsFromThreatRange
    lazy val excessDistanceFromTarget = currentPixelsFromTargetRange - idealPixelsFromTargetRange
    lazy val tooCloseToThreat = missingDistanceFromThreat >= 0
    lazy val tooFarFromTarget = excessDistanceFromTarget > 0

    lazy val purring = (unit.unitClass.isTerran && unit.unitClass.isMechanical && unit.matchups.allies.exists(a => a.repairing && a.orderTarget.contains(unit)))
  }

  def simplePotshot(context: MicroContext): Unit = {
    if (context.unit.unready) return
    val oldToAttack = context.unit.agent.toAttack
    context.unit.agent.toAttack = None
    // TODO: Handle extant targets
    PotshotTarget.delegate(context.unit)
    With.commander.attack(context.unit)
    context.unit.agent.toAttack = context.unit.agent.toAttack.orElse(oldToAttack)
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

  protected def micro(unit: FriendlyUnitInfo, shouldEngage: Boolean): Unit = {
    val context = new MicroContext(unit, shouldEngage)

    // AIM: If we can't move, just fire at a target
    if ( ! unit.canMove) {
      unit.agent.act("Aim")
      aimInPlace(context)
      return
    }

    // DODGE: Avoid explosions
    if (context.receivedPushPriority >= TrafficPriorities.Dodge) {
      unit.agent.act("Dodge")
      followPushing(context)
      return
    }

    // PURR: If we're getting repaired, stand still and let it happen
    if (context.purring) {
      unit.agent.act("Purr")
      aimInPlace(context)
      return
    }

    // Fliers: Use group formation
    // TODO

    // Evaluate potential attacks
    context.retarget()

    if (Brawl.consider(unit)) return

    // BATTER
    // TODO

    // Back up if we need to
    if (context.tooCloseToThreat) {
      // ENGAGE: Kite if appropriate
      if (shouldEngage) {
        // TODO: BREATHE

        // ABUSE/BREATHE
        lazy val framesToOpenGap = PurpleMath.nanToInfinity(context.missingDistanceFromThreat / unit.topSpeed)
        if (context.excessDistanceFromTarget < -24
          && context.idealPixelsFromTargetRange > context.idealPixelsFromThreatRange
          && (
            // Breathe
           unit.readyForAttackOrder
            // Abuse
            || unit.matchups.threats.forall(threat =>
              threat.topSpeed < unit.topSpeed
              || threat.framesToGetInRange(unit) > framesToOpenGap
              || ( ! threat.inRangeToAttack(unit) && (threat.speedApproaching(unit) < 0 || threat.target.exists(_ != unit)))))) {
          unit.agent.act("Kite")
          Retreat.consider(unit)
          return
        }

        // BREATHE, sort of
        // TODO: Account for 2x 180 time
        if (unit.readyForAttackOrder && context.tooCloseToThreat && context.excessDistanceFromTarget < -24) {
          Retreat.consider(unit)
          unit.agent.act("Breathe")
          return
        }
      }
      // DISENGAGE: Run away!
      else {
        // FALLBACK
        // If getting shots off while retreating is easy, go for it
        if (
          unit.isAny(Terran.Vulture, Terran.SiegeTankUnsieged, Terran.Goliath, Terran.Wraith, Protoss.Archon, Protoss.Dragoon, Protoss.Reaver, Protoss.Scout, Zerg.Hydralisk, Zerg.Mutalisk)
          && unit.readyForAttackOrder
          && ( unit.is(Protoss.Reaver) || context.receivedPushPriority < TrafficPriorities.Shove)
          && ( ! unit.isAny(Protoss.Archon, Protoss.Dragoon) || ! unit.matchups.threats.exists(t => t.is(Protoss.Dragoon) && t.framesToGetInRange(t) < 12))) {
          simplePotshot(context)
          unit.agent.act("Fallback")
        }
        Retreat.consider(unit)
        return
      }
    }


    // Shoves: Follow and propagate them
    if (context.receivedPushPriority >= TrafficPriorities.Shove) {
      // TODO: Do we actually want to follow the shove?
      // Maybe the push is sending us where we want to go anyway,
      // or the push is coming from a less important unit
      // or the push is from a less-engaged unit in the previous agent cycle
      // *Decide* whether pushing changes our behavior
      unit.agent.act("Shoved")
      simplePotshot(context)
      followPushing(context)
      return
    }

    // If we have a target, use firing position as destination
    // TODO: Find smarter firing positions. Find somewhere safe and unoccupied but not too far to stand.
    // TODO: If we're retreating, destination should be origin
    unit.agent.toTravel = unit.agent.toAttack.map(_.pixelCenter).filter(p => With.grids.walkable.get(p.tileIncluding) || unit.flying).orElse(unit.agent.toTravel)
    val forces = unit.agent.forces
    val destination = unit.agent.destination
    val destinationDistance = unit.pixelDistanceTravelling(destination)
    forces(Forces.traveling)  = Potential.preferTravel(unit, destination)
    forces(Forces.threat)     = Potential.avoidThreats(unit)
    forces(Forces.spacing)    = Potential.avoidCollision(unit)
    forces(Forces.spreading)  = MicroPathing.getPushRadians(context.receivedPushForces).map(ForceMath.fromRadians(_)).getOrElse(new Force)
    // TODO: avoid splash
    // TODO: forceRegroup, forceCohesion
    // Reference https://github.com/bmnielsen/Stardust/blob/master/src/General/UnitCluster/Tactics/Move.cpp#L69

    if (shouldEngage) {
      // Nudge through if we want to reach a target
      if ( ! unit.flying && unit.agent.toAttack.exists( ! unit.inRangeToAttack(_))) {
        unit.agent.escalatePriority(TrafficPriorities.Nudge)
      }

      if (context.tooFarFromTarget) {
        forces(Forces.traveling) *= 2
      }
      if ( ! context.tooCloseToThreat) {
        forces(Forces.threat) /= 2
      }
    } else {
      if (context.tooCloseToThreat) {
        // This case is already handled by retreat logic
        unit.agent.escalatePriority(TrafficPriorities.Shove)
      }
      else if (context.tooFarFromTarget) {
        forces(Forces.threat) /= 2
      } else {
        forces(Forces.traveling) /= 2
      }
    }

    // REPOSITION: Reposition if we want to
    // - Do we want to adjust our distance?
    // - Is there a place we could hit our target while eating less damage?
    // - Can we open room for teammates behind us to enter?
    // TODO

    if (unit.agent.toAttack.exists(unit.pixelsToGetInRange(_) < 64) && (unit.unitClass.melee || unit.readyForAttackOrder)) {
      With.commander.attack(unit)
      unit.agent.act("ComAttack")
      return
    }

    // TODO: CHASE: Moving shot/pursue if we want to
    val groupTravel = MicroPathing.findRayTowards(unit, unit.agent.force.radians)

    if (groupTravel.isDefined) {
      unit.agent.toTravel = groupTravel
      unit.agent.act("ComMove")
      With.commander.move(unit)
      return
    }
  }
}
