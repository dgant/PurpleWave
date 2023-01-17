package Macro.Scheduling

import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.Upgrades.Upgrade
import Utilities.CloneyCountMap

final class MacroState {
  var minerals          : Int = _
  var gas               : Int = _
  var supplyAvailable   : Int = _
  var supplyUsed        : Int = _
  var mineralPatches    : Int = _
  var geysers           : Int = _
  var techs             : Set[Tech]                 = Set.empty
  var upgrades          : CloneyCountMap[Upgrade]   = new CloneyCountMap[Upgrade]
  var unitsExtant       : CloneyCountMap[UnitClass] = new CloneyCountMap[UnitClass]
  var unitsComplete     : CloneyCountMap[UnitClass] = new CloneyCountMap[UnitClass]
  var unitsCompleteASAP : CloneyCountMap[UnitClass] = new CloneyCountMap[UnitClass] // Counts only units completed without a minStartFrame; eg if we want 3 pylons ASAP and 3 have been queued but the last has a minStartFrame, queue it anyway
  var producers         : CloneyCountMap[UnitClass] = new CloneyCountMap[UnitClass]
}
