package Micro.Actions.Combat.Targeting.Filters

import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterAnchor extends TargetFilter {
  // Think about this
  // simulationSafe = true
  override def appliesTo(actor: FriendlyUnitInfo): Boolean = actor.matchups.anchor.isDefined
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    val anchors = actor.matchups.anchors
    var output = false
    output ||= actor.inRangeToAttack(target) && actor.readyForAttackOrder
    output ||= anchors.exists(target.inRangeToAttack)
    output ||= anchors.exists(_.inRangeToAttack(target))
    output ||= anchors.exists(a => a.pixelDistanceEdge(target) < Math.max(96, Math.max(a.effectiveRangePixels, target.effectiveRangePixels)))
    output
  }
}
