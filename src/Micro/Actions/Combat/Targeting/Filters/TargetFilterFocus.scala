package Micro.Actions.Combat.Targeting.Filters

import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterFocus extends TargetFilter {
  
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = (
    ! actor.agent.canFocus
    || actor.agent.destination.zone == target.zone
    || actor.base.exists(b => b.owner.isEnemy && actor.matchups.threats.forall(_.unitClass.isWorker))
    || actor.inRangeToAttack(target)
    || (target.canAttack(actor) && target.inRangeToAttack(actor))
    || actor.agent.focusPathSteps.exists(tile =>
      target.pixelDistanceCenter(tile.pixelCenter)
      < Math.max(
          actor.pixelRangeAgainst(target),
          if (target.canAttack(actor)) target.pixelRangeAgainst(actor) else 0)
      + 32 * actor.agent.focusPathStepSize)
    || target.interceptors.exists(legal(actor, _))
  )
}
