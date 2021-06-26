package Micro.Targeting.FiltersRequired

import Lifecycle.With
import Micro.Targeting.TargetFilter
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterReaver extends TargetFilter {
  simulationSafe = true
  override def appliesTo(actor: FriendlyUnitInfo): Boolean = Protoss.Reaver(actor)
  override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    if (actor.agent.ride.isEmpty) return true

    // Don't try to snipe tanks that will kill us first
    if (actor.loaded
      && Terran.SiegeTankSieged(target)
      && target.cooldownLeft < Math.max(actor.cooldownLeft, With.game.getRemainingLatencyFrames)
      && target.matchups.targetsInRange.forall(_ == actor)) {
      return false
    }

    // Don't hit eg. random buildings in bases
    val worthAttacking = (
      actor.base.exists(_.owner.isUs) // Make sure we hit Pylons or whatever other garbage is around
      || target.canAttack
      || target.unitClass.attacksOrCastsOrDetectsOrTransports
      || (target.base.exists(b => b.resources.forall(u => u.tile.visible)) && target.base.forall(_.workerCount == 0)))

    val safeToAttack = (
      ! target.canAttack(actor)
      || target.pixelRangeAgainst(actor) < actor.pixelRangeAgainst(target)
      || ( ! actor.loaded && actor.inRangeToAttack(target)))

    worthAttacking && safeToAttack
  }
}
