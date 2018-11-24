package Micro.Actions.Combat.Targeting.Filters

import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterFocus extends TargetFilter {
  
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = (
    ! actor.agent.canFocus
    || target.zone == actor.agent.destination.zone
    || (target.canAttack(actor) && target.inRangeToAttack(actor))
    || (target.visible && target.topSpeed < actor.topSpeed * 0.75 && actor.matchups.threats.isEmpty)
  )
  
}
