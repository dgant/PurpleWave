package Micro.Targeting.FiltersRequired

import Lifecycle.With
import Micro.Targeting.TargetFilter
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterFocus extends TargetFilter {
  simulationSafe = false
  override def appliesTo(actor: FriendlyUnitInfo): Boolean = actor.squad.exists(_.targetQueue.isDefined)
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    if (actor.agent.commit) return true
    if (With.yolo.active()) return true
    if (actor.inRangeToAttack(target) && target.unitClass.attacksOrCastsOrDetectsOrTransports) return true
    actor.squad.forall(_.targetQueue.forall(_.contains(target)))
  }
}
