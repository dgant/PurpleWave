package Micro.Targeting.FiltersRequired

import Lifecycle.With
import Micro.Targeting.TargetFilter
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterFocus extends TargetFilter {
  simulationSafe = false
  override def appliesTo(actor: FriendlyUnitInfo): Boolean = actor.targetsAssigned.isDefined
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    if (actor.agent.commit) return true
    if (With.yolo.active) return true
    if (actor.inRangeToAttack(target)
      && actor.readyForAttackOrder
      && (target.unitClass.attacksOrCastsOrDetectsOrTransports || ! actor.squad.exists(_.engagedUpon))) return true
    actor.targetsAssigned.forall(_.contains(target))
  }
}
