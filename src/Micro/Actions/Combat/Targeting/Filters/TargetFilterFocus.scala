package Micro.Actions.Combat.Targeting.Filters

import Lifecycle.With
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterFocus extends TargetFilter {
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = (
    (actor.inRangeToAttack(target) && actor.readyForAttackOrder && target.matchups.targets.nonEmpty)
    || With.yolo.active()
    || actor.squad.forall(target.squads.contains)
    || (actor.topSpeed > target.topSpeed && actor.pixelDistanceTravelling(actor.agent.destination) >= actor.pixelToFireAt(target).travelPixelsFor(actor.agent.destination, actor))
  )
}
