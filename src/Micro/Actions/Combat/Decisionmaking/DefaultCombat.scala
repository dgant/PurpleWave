package Micro.Actions.Combat.Decisionmaking

import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Actions.Combat.Tactics.Brawl
import Micro.Actions.Combat.Tactics.Potshot.PotshotTarget
import Micro.Actions.Combat.Targeting.Filters.TargetFilter
import Micro.Actions.Combat.Targeting.{EvaluateTargets, TargetInRange}
import Micro.Actions.Commands.{Attack, Move}
import Micro.Coordination.Pathing.MicroPathing
import Micro.Coordination.Pushing.{TrafficPriorities, TrafficPriority}
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

    lazy val pushes = With.coordinator.pushes.get(unit).map(p => (p, p.force(unit))).sortBy(-_._1.priority.value)
    lazy val pushPriority: TrafficPriority = ByOption.max(pushes.view.map(_._1.priority)).getOrElse(TrafficPriorities.None)

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

  def simplePotshot(context: MicroContext): Boolean = {
    val oldToAttack = context.unit.agent.toAttack
    context.unit.agent.toAttack = None
    // TODO: Handle extant targets
    PotshotTarget.delegate(context.unit)
    Attack.delegate(context.unit)
    context.unit.agent.toAttack = context.unit.agent.toAttack.orElse(oldToAttack)
    ! context.unit.ready
  }

  def propagatePush(context: MicroContext): Unit = {
    // TODO
    // Push along our expected path with the same priority
    //
    // Signal occupancy along expected path
    // path.tiles.get.foreach(With.coordinator.gridPathOccupancy.addUnit(unit, _))
  }

  def followPush(context: MicroContext): Unit = {
    val force = MicroPathing.getPushForce(context.unit)

  }

  def aimInPlace(context: MicroContext): Unit = {
    TargetInRange.delegate(context.unit)
    Attack.delegate(context.unit)
    With.commander.hold(context.unit)
  }

  protected def micro(unit: FriendlyUnitInfo, shouldEngage: Boolean): Unit = {
    val context = new MicroContext(unit, shouldEngage)

    // AIM: If we can't move, just fire at a target
    if ( ! unit.canMove) {
      aimInPlace(context)
      return
    }

    // DODGE: Avoid explosions
    if (context.pushPriority >= TrafficPriorities.Dodge) {
      followPush(context)
      return
    }

    // Shoves: Follow and propagate them
    if (context.pushPriority >= TrafficPriorities.Dodge) {
      // TODO: Do we actually want to follow the shove?
      // Maybe the push is sending us where we want to go anyway,
      // or the push is coming from a less important unit
      // or the push is from a less-engaged unit in the previous agent cycle
      // *Decide* whether pushing changes our behavior
      if (simplePotshot(context)) return
      followPush(context)
      return
    }

    // PURR: If we're getting repaired, stand still and let it happen
    if (context.purring) {
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
    // TODO: Bump when avoiding
    if (context.tooCloseToThreat) {
      if (shouldEngage) {
        // BREATHE
        // TODO

        // ABUSE/BREATHE
        lazy val framesToOpenGap = PurpleMath.nanToInfinity(context.missingDistanceFromThreat / unit.topSpeed)
        if (context.excessDistanceFromTarget < -24
          && context.idealPixelsFromTargetRange > context.idealPixelsFromThreatRange
          && (
            // Breathe
            ! unit.readyForAttackOrder
            // Abuse
            || unit.matchups.threats.forall(threat =>
              threat.topSpeed < unit.topSpeed
              || threat.framesToGetInRange(unit) > framesToOpenGap
              || ( ! threat.inRangeToAttack(unit) && (threat.speedApproaching(unit) < 0 || threat.target.exists(_ != unit)))))) {
          Retreat.consider(unit)
        }

        // BREATHE, sort of
        // TODO: Account for 2x 180 time
        if ( ! unit.readyForAttackOrder && context.tooCloseToThreat && context.excessDistanceFromTarget < -24) {
          Retreat.consider(unit)
        }
      } else {
        // Don't IGNORE
        Retreat.consider(unit)
      }
    }

    // REPOSITION: Reposition if we want to
    // - Do we want to adjust our distance?
    // - Is there a place we could hit our target while eating less damage?
    // - Can we open room for teammates behind us to enter?
    // TODO

    // TODO: When we have an ideal distance, only move/shove up to that distance and no further

    // CHASE: Moving shot/pursue if we want to
    // TODO

    // CHARGE
    // TODO: Nudge when attacking something out of range or attacking
    // TODO: Find position to shoot when distant
    if (shouldEngage || unit.agent.toAttack.exists(unit.inRangeToAttack)) {
      Attack.consider(unit)
    }

    // Explicitly move, since we might have a target that we've simply chosen not to attack
    // TODO: Squad movement
    Move.consider(unit)
  }
}
