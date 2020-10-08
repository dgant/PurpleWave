package Micro.Actions.Combat.Decisionmaking

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Tactics.Potshot.PotshotTarget
import Micro.Actions.Combat.Targeting.Target
import Micro.Actions.Combat.Techniques.{Aim, Avoid}
import Micro.Actions.Commands.{Attack, Move}
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.{ByOption, LightYear}

object EngageDisengage extends Action {
  override def allowed(unit: FriendlyUnitInfo): Boolean = true

  object NewEngage extends Action {
    override def allowed(unit: FriendlyUnitInfo): Boolean = true
    override protected def perform(unit: FriendlyUnitInfo): Unit = EngageDisengage.micro(unit, shouldEngage = true)
  }

  object NewDisengage extends Action {
    override def allowed(unit: FriendlyUnitInfo): Boolean = true
    override protected def perform(unit: FriendlyUnitInfo): Unit = EngageDisengage.micro(unit, shouldEngage = false)
  }

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    micro(unit, unit.agent.shouldEngage)
  }

  def micro(unit: FriendlyUnitInfo, shouldEngage: Boolean): Unit = {
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
    if ( ! unit.canMove) {
      if (Aim.consider(unit)) return
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
    val idealPixelsFromTarget = target.map(unit.pixelRangeAgainst).getOrElse(unit.effectiveRangePixels)
    val idealPixelsFromThreat = ByOption.max(unit.matchups.threats.view.map(64d + _.pixelRangeAgainst(unit))).getOrElse(0d) // TODO: Increase for abusable threat-targets

    // Decide how far from adjacent teammates we want to be
    val idealPixelsFromTeammatesCollision = if (unit.flying) 0 else unit.unitClass.dimensionMax / 2
    val idealPixelsFromTeammatesSplash = ByOption.max(unit.matchups.threats.view.map(_.unitClass).filter(_.dealsRadialSplashDamage).map(t => 1 + (if (unit.flying) t.airSplashRadius25 else t.groundSplashRadius25))).getOrElse(0)
    val idealPixelsFromTeammates = Math.max(idealPixelsFromTeammatesCollision, idealPixelsFromTeammatesSplash)

    // Check how far from target/threat/teammate we are
    val currentPixelsFromThreat = ByOption.min(unit.matchups.threats.view.map(_.pixelsToGetInRange(unit))).getOrElse(LightYear().toDouble)
    val currentPixelsFromTarget = target.map(unit.pixelsToGetInRange).getOrElse(unit.pixelDistanceTravelling(unit.agent.destination))
    val currentPixelsFromTeammate = unit.matchups.allies.view.map(_.pixelDistanceEdge(unit))

    val missingDistanceFromThreat = idealPixelsFromThreat - currentPixelsFromThreat
    val excessDistanceFromTarget = currentPixelsFromTarget - idealPixelsFromTarget
    val tooCloseToThreat = missingDistanceFromThreat >= 0
    val tooFarFromTarget = excessDistanceFromTarget > 0

    // BRAWL: If we're in a melee mess
    // TODO

    // BREATHE: Shoot if we want to
    if (shouldEngage && target.isDefined && unit.framesToGetInRange(target.get) + With.reaction.agencyAverage + unit.unitClass.framesToTurn180 >= unit.cooldownLeft) {
      Attack.delegate(unit)
    }

    // FALLBACK: Shoot if we want to
    if ( ! shouldEngage && unit.isAny(Terran.SiegeTankUnsieged, Terran.Goliath, Protoss.Dragoon)) {
      simplePotshot()
    }

    // Back up if we need to.
    // TODO: Shove when avoiding
    // TODO: When we have an ideal distance, only move/shove up to that distance and no further
    if (tooCloseToThreat) {
      if (shouldEngage) {
        // BREATHE
        val turnaroundFrames = unit.unitClass.framesToTurn180 + excessDistanceFromTarget * unit.topSpeed
        if (unit.cooldownLeft > turnaroundFrames) {
          Avoid.consider(unit)
        }
        // ABUSE
        // TODO
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
