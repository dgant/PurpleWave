package Micro.Actions.Combat.Targeting.Filters

import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

trait TargetFilter {
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean
}
