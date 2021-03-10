package Micro.Actions.Combat.Targeting.Filters

import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

trait TargetFilter {
  var simulationSafe: Boolean = false
  def appliesTo(actor: FriendlyUnitInfo): Boolean = true
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean
}
