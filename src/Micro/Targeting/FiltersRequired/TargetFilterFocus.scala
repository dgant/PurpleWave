package Micro.Targeting.FiltersRequired

import Lifecycle.With
import Micro.Targeting.TargetFilter
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterFocus extends TargetFilter {
  override def appliesTo(actor: FriendlyUnitInfo): Boolean = actor.targetsAssigned.isDefined
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    var output = false
    output ||= actor.agent.commit
    output ||= With.yolo.active
    output ||= actor.squad.exists(_.engagedUpon) && target.team.exists(_.engagingOn)
    output ||= actor.targetsAssigned.forall(_.contains(target))
    output ||= actor.squad.exists(s => s.confidence11 < 0.2 && s.roadblocks.contains(target))
    output ||= (actor.inRangeToAttack(target)
      && actor.readyForAttackOrder
      && actor.framesToFace(target) <= 2
      && target.canAttack(actor))
    output ||= ! actor.flying && ! target.flying && actor.pixelDistanceEdge(target) < 96
    output ||= ! actor.canMove

    output
  }
}
