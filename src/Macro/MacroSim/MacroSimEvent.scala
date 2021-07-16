package Macro.MacroSim

import ProxyBwapi.Techs.{Tech, Techs}
import ProxyBwapi.UnitClasses.{UnitClass, UnitClasses}
import ProxyBwapi.Upgrades.{Upgrade, Upgrades}

final class MacroSimEvent {
  var exists: Boolean = _
  var dFrames: Int = _
  var dMinerals: Int = _
  var dGas: Int = _
  var dSupplyAvailable: Int = _
  var dSupplyUsed: Int = _
  var dMineralPatches: Int = _
  var dGeysers: Int = _
  var dTech: Tech = _
  var dUpgrade: Upgrade = _
  var dUnit1: UnitClass = _
  var dUnit1N: Int = _
  var dUnit2: UnitClass = _
  var dUnit2N: Int = _
  def reset(): Unit = {
    exists = false
    dFrames = 0
    dMinerals = 0
    dGas = 0
    dSupplyAvailable = 0
    dSupplyUsed = 0
    dMineralPatches = 0
    dGeysers = 0
    dTech = Techs.None
    dUpgrade = Upgrades.None
    dUnit1 = UnitClasses.None
    dUnit1N = 0
    dUnit2 = UnitClasses.None
    dUnit2N = 0
  }
  reset()
}
