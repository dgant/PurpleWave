package Micro.Targeting

import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

trait TargetFilter {
  def appliesTo(actor: FriendlyUnitInfo): Boolean = true
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean
}
