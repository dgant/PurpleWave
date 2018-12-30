package Micro.Actions.Combat.Targeting.Filters
import Lifecycle.With
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterReaver extends TargetFilter {
  override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    if ( ! actor.isReaver() || actor.agent.ride.isEmpty) return true

    val worthAttacking = (
      target.canAttack
      || target.unitClass.attacks
      || target.base.exists(b => With.grids.friendlyVision.isSet(b.heart)))
    val safeToAttack = (
      ! target.canAttack(actor)
      || target.pixelRangeAgainst(actor) < actor.pixelRangeAgainst(target)
      || ( ! actor.loaded && actor.inRangeToAttack(target)))

    worthAttacking && safeToAttack
  }
}
