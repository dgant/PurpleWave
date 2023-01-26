package Micro.Targeting.FiltersRequired

import Lifecycle.With
import Micro.Targeting.TargetFilter
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterFocus extends TargetFilter {
  simulationSafe = true
  override def appliesTo(actor: FriendlyUnitInfo): Boolean = actor.targetsAssigned.isDefined
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    if (actor.agent.commit) return true
    if (With.yolo.active) return true
    if (actor.inRangeToAttack(target)
      && actor.readyForAttackOrder
      && actor.framesToFace(target) <= 2
      && (target.unitClass.attacksOrCastsOrDetectsOrTransports || ! actor.squad.exists(_.engagedUpon))) return true
    // If our focus includes the target
    if (actor.targetsAssigned.forall(_.contains(target))) return true
    // If the target is RIGHT IN OUR WAY
    if ( ! actor.flying && ! target.flying && actor.pixelDistanceEdge(target) < 96) return true
    false
  }
}
