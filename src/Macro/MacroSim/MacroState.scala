package Macro.MacroSim

import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.Upgrades.Upgrade
import Utilities.CountMap

final class MacroState {
  var minerals: Int = _
  var gas: Int = _
  var supplyAvailable: Int = _
  var supplyUsed: Int = _
  var mineralPatches: Int = _
  var geysers: Int = _
  var techs: Set[Tech] = Set.empty
  var upgrades =  new CountMap[Upgrade]
  var units = new CountMap[UnitClass]
  var producers = new CountMap[UnitClass]
}
