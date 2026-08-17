package Micro.Targeting.FiltersRequired

import Lifecycle.With
import Micro.Targeting.TargetFilter
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterReaver extends TargetFilter {
  override def appliesTo(actor: FriendlyUnitInfo): Boolean = Protoss.Reaver(actor)
  override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {

    if (actor.agent.ride.isDefined) {
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

      if ( ! worthAttacking) return false

      val safeToAttack = (
        ! target.canAttack(actor)
        || target.pixelRangeAgainst(actor) < actor.pixelRangeAgainst(target)
        || ( ! actor.loaded && actor.inRangeToAttack(target)))

      if ( ! safeToAttack) return false
    }

    if (actor.inRangeToAttack(target))                      return true
    if (actor.matchups.threatNearest.contains(target))      return true
    if (actor.matchups.pixelsToThreatRange.forall(_ > 96))  return true

    false
  }
}
