package Micro.Targeting.Filters

import Micro.Agency.AnchorMargin
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterAnchor extends TargetFilter {
  // Think about this
  // simulationSafe = true
  override def appliesTo(actor: FriendlyUnitInfo): Boolean = actor.matchups.anchor.isDefined
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    val anchors = actor.matchups.anchors
    var output = false
    output ||= actor.inRangeToAttack(target) && actor.readyForAttackOrder
    output ||= anchors.exists(a => target.canAttack(a) && target.pixelDistanceEdge(a) < target.pixelRangeAgainst(a) + AnchorMargin(actor))
    output ||= anchors.exists(a => a.pixelDistanceEdge(target) < a.effectiveRangePixels + AnchorMargin(actor))
    output
  }
}
