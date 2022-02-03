package Macro.MacroSim

import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitInfo.UnitInfo
import ProxyBwapi.Upgrades.Upgrade

final class MacroRequest {
  var unit: Option[UnitInfo] = None
  var upgrade: Option[Upgrade] = None
  var tech: Option[Tech] = None
  var min: Int = 1
  var max: Int = 400
  // TODO: Placement details
}
