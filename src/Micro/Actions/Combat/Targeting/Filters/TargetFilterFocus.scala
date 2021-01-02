package Micro.Actions.Combat.Targeting.Filters

import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterFocus extends TargetFilter {
  override def appliesTo(actor: FriendlyUnitInfo): Boolean = actor.agent.canFocus
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = (
    ! actor.agent.canFocus
    || actor.agent.destination.zone == target.zone
    || actor.inRangeToAttack(target)
    || (target.canAttack(actor) && target.inRangeToAttack(actor))
    || actor.base.exists(b => b.owner.isEnemy && actor.matchups.threats.forall(_.unitClass.isWorker))
    // TODO: This logic was modified untested when replacing micro code and removing "focus paths"
    || (if (actor.flying) actor.pixelToFireAt(target).pixelDistance(actor.agent.destination) else actor.pixelToFireAt(target).nearestWalkableTile.groundPixels(actor.agent.destination)) <= actor.pixelDistanceTravelling(actor.agent.destination)
  )
}
