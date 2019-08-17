package Micro.Actions.Combat.Targeting.Filters

import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterFocus extends TargetFilter {
  
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = (
    ! actor.agent.canFocus
    || actor.agent.destination.zone == target.zone
    || actor.base.exists(b => b.owner.isEnemy && actor.matchups.threats.forall(_.unitClass.isWorker))
    || actor.inRangeToAttack(target)
    || (target.canAttack(actor) && target.inRangeToAttack(actor))
    || (target.visible && target.topSpeed < actor.topSpeed * 0.75 && actor.matchups.threats.isEmpty)
    || target.interceptors.exists(legal(actor, _))
  )
  
}
