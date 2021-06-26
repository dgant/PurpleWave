package Micro.Targeting.FiltersRequired

import Micro.Targeting.TargetFilter
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterFocus extends TargetFilter {
  simulationSafe = true
  override def appliesTo(actor: FriendlyUnitInfo): Boolean = actor.squad.exists(_.targetQueue.isDefined)
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = (
    (actor.inRangeToAttack(target) && actor.readyForAttackOrder && target.unitClass.attacksOrCastsOrDetectsOrTransports)
    || target.pixelDistanceTravelling(actor.agent.destination) <= actor.pixelDistanceTravelling(actor.agent.destination)
  )
}
