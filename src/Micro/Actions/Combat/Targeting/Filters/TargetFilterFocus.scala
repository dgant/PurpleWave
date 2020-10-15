package Micro.Actions.Combat.Targeting.Filters

import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterFocus extends TargetFilter {
  
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = (
    ! actor.agent.canFocus
    || actor.agent.destination.zone == target.zone
    || actor.base.exists(b => b.owner.isEnemy && actor.matchups.threats.forall(_.unitClass.isWorker))
    || actor.inRangeToAttack(target)
    || (target.canAttack(actor) && target.inRangeToAttack(actor))
    || target.interceptors.exists(legal(actor, _))
    // TODO: This logic was modified untested when replacing micro code and removing "focus paths"
    || (if (actor.flying) actor.pixelToFireAt(target).pixelDistance(actor.agent.destination) else actor.pixelToFireAt(target).nearestWalkableTerrain.groundPixels(actor.agent.destination)) <= actor.pixelDistanceTravelling(actor.agent.destination)
  )
}
