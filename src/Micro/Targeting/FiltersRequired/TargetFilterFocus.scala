package Micro.Targeting.FiltersRequired

import Micro.Targeting.TargetFilter
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterFocus extends TargetFilter {
  simulationSafe = false
  override def appliesTo(actor: FriendlyUnitInfo): Boolean = actor.squad.exists(_.targetQueue.isDefined)
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    if (actor.inRangeToAttack(target) && target.unitClass.attacksOrCastsOrDetectsOrTransports) return true
    actor.squad.forall(_.targetQueue.forall(_.contains(target)))
  }
}
