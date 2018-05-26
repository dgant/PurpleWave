package Micro.Actions.Combat.Targeting.Filters

import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

case class TargetFilterWhitelist(units: Iterable[UnitInfo]) extends TargetFilter {
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    units.exists(_ == target)
  }
}
