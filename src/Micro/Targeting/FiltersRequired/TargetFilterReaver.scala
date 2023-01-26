package Micro.Targeting.FiltersRequired

import Lifecycle.With
import Micro.Targeting.TargetFilter
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterReaver extends TargetFilter {
  simulationSafe = true
  override def appliesTo(actor: FriendlyUnitInfo): Boolean = Protoss.Reaver(actor) && actor.agent.ride.isDefined
  override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {

    // Don't try to snipe tanks that will kill us first
    if (actor.loaded
      && Terran.SiegeTankSieged(target)
      && target.cooldownLeft < Math.max(actor.cooldownLeft, With.game.getRemainingLatencyFrames)
      && target.matchups.targetsInRange.forall(actor==)) {
      return false
    }

    // Don't hit eg. random buildings in bases
    val worthAttacking = (
      target.proxied
      || target.unitClass.attacksOrCastsOrDetectsOrTransports
      || target.base.exists(b => b.workerCount == 0 && b.resources.forall(u => u.tile.visible)))

    val safeToAttack = (
      ! target.canAttack(actor)
      || target.pixelRangeAgainst(actor) < actor.pixelRangeAgainst(target)
      || ( ! actor.loaded && actor.inRangeToAttack(target)))

    worthAttacking && safeToAttack
  }
}
