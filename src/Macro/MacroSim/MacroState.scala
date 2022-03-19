package Macro.MacroSim

import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.Upgrades.Upgrade
import Utilities.CountMap

final class MacroState {
  var minerals        : Int = _
  var gas             : Int = _
  var supplyAvailable : Int = _
  var supplyUsed      : Int = _
  var mineralPatches  : Int = _
  var geysers         : Int = _
  var techs           : Set[Tech]           = Set.empty
  var upgrades        : CountMap[Upgrade]   = new CountMap[Upgrade]
  var unitsExtant     : CountMap[UnitClass] = new CountMap[UnitClass]
  var unitsComplete   : CountMap[UnitClass] = new CountMap[UnitClass]
  var producers       : CountMap[UnitClass] = new CountMap[UnitClass]
}
