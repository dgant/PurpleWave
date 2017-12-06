package Micro.Actions.Combat.Attacking.Filters

import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

trait TargetFilter {
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean
}
