package Micro.Actions.Combat.Targeting.Filters
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterReaver extends TargetFilter {
  override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    if ( ! actor.isReaver() || actor.agent.ride.isEmpty) return true

    // Don't hit eg. random buildings in bases
    val worthAttacking = (
      actor.base.exists(_.owner.isUs) // Make sure we hit Pylons or whatever other garbage is around
      || target.canAttack
      || target.unitClass.canAttack
      || (
        target.base.exists(b => b.resources.forall(u => u.tile.visible))
        && target.base.forall(_.workerCount == 0)))

    val safeToAttack = (
      ! target.canAttack(actor)
      || target.pixelRangeAgainst(actor) < actor.pixelRangeAgainst(target)
      || ( ! actor.loaded && actor.inRangeToAttack(target)))

    worthAttacking && safeToAttack
  }
}
