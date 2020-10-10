package Micro.Actions.Combat.Decisionmaking

import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Avoid
import Micro.Actions.Combat.Tactics.Brawl
import Micro.Actions.Combat.Tactics.Potshot.PotshotTarget
import Micro.Actions.Combat.Targeting.{Target, TargetInRange}
import Micro.Actions.Commands.{Attack, Move}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
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

  protected def micro(unit: FriendlyUnitInfo, shouldEngage: Boolean): Unit = {
    def target = { unit.agent.toAttack }
    def simplePotshot(): Boolean = {
      val oldToAttack = unit.agent.toAttack
      unit.agent.toAttack = None
      PotshotTarget.delegate(unit)
      Attack.delegate(unit)
      unit.agent.toAttack = unit.agent.toAttack.orElse(oldToAttack)
      ! unit.ready
    }

    // AIM: If we can't move, just fire at a target
    // PURR: If we're getting repaired, stand still and enjoy
    lazy val purring = (unit.unitClass.isTerran && unit.unitClass.isMechanical && unit.matchups.allies.exists(a => a.repairing && a.orderTarget.contains(unit)))
    if ( ! unit.canMove || purring) {
      TargetInRange.delegate(unit)
      Attack.delegate(unit)
      With.commander.sleep(unit)
      return
    }

    // Explosions: Dodge them
    // TODO

    // Shoves: Follow and propagate them
    // TODO

    // Fliers: Use group formation
    // TODO

    // Evaluate potential attacks
    // TODO: Evaluate all targets once and cache values,
    // then pick best target given filters
    Target.consider(unit)

    // Decide how far from target/threat we want to be
    val idealPixelsFromTargetRange = if (target.exists(_.speedApproaching(unit) < 0)) -16d else 0d
    val idealPixelsFromThreatRange = 64 + Math.max(0, unit.effectiveRangePixels - ByOption.min(unit.matchups.allies.view.map(_.effectiveRangePixels)).getOrElse(unit.effectiveRangePixels)) / 4 // Induce sorting

    // Decide how far from adjacent teammates we want to be
    val idealPixelsFromTeammatesCollision = if (unit.flying) 0 else unit.unitClass.dimensionMax / 2
    val idealPixelsFromTeammatesSplash = ByOption.max(unit.matchups.threats.view.map(_.unitClass).filter(_.dealsRadialSplashDamage).map(t => 1 + (if (unit.flying) t.airSplashRadius25 else t.groundSplashRadius25))).getOrElse(0)
    val idealPixelsFromTeammates = Math.max(idealPixelsFromTeammatesCollision, idealPixelsFromTeammatesSplash)

    // Check how far from target/threat/teammate we are
    val currentPixelsFromThreatRange = ByOption.min(unit.matchups.threats.view.map(_.pixelsToGetInRange(unit))).getOrElse(LightYear().toDouble)
    val currentPixelsFromTargetRange = target.map(unit.pixelsToGetInRange).getOrElse(unit.pixelDistanceTravelling(unit.agent.destination))
    val currentPixelsFromTeammate = unit.matchups.allies.view.map(_.pixelDistanceEdge(unit))

    val missingDistanceFromThreat = idealPixelsFromThreatRange - currentPixelsFromThreatRange
    val excessDistanceFromTarget = currentPixelsFromTargetRange - idealPixelsFromTargetRange
    val tooCloseToThreat = missingDistanceFromThreat >= 0
    val tooFarFromTarget = excessDistanceFromTarget > 0

    if (Brawl.consider(unit)) return

    // BATTER
    // TODO

    // Back up if we need to
    // TODO: Bump when avoiding
    if (tooCloseToThreat) {
      if (shouldEngage) {
        // BREATHE
        // TODO

        // ABUSE/BREATHE
        lazy val framesToOpenGap = PurpleMath.nanToInfinity(missingDistanceFromThreat / unit.topSpeed)
        if (excessDistanceFromTarget < -24
          && idealPixelsFromTargetRange > idealPixelsFromThreatRange
          && (
            // Breathe
            ! unit.readyForAttackOrder
            // Abuse
            || unit.matchups.threats.forall(threat =>
              threat.topSpeed < unit.topSpeed
              || threat.framesToGetInRange(unit) > framesToOpenGap
              || ( ! threat.inRangeToAttack(unit) && (threat.speedApproaching(unit) < 0 || threat.target.exists(_ != unit)))))) {
          Avoid.consider(unit)
        }

        // BREATHE, sort of
        // TODO: Account for 2x 180 time
        if ( ! unit.readyForAttackOrder && tooCloseToThreat && excessDistanceFromTarget < -24) {
          Avoid.consider(unit)
        }
      } else {
        // Don't IGNORE
        Avoid.consider(unit)
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
    if (shouldEngage || target.exists(unit.inRangeToAttack)) {
      Attack.consider(unit)
    }

    // Explicitly move, since we might have a target that we've simply chosen not to attack
    // TODO: Squad movement
    Move.consider(unit)
  }
}
