package Macro.MacroSim

import ProxyBwapi.Techs.{Tech, Techs}
import ProxyBwapi.UnitClasses.{UnitClass, UnitClasses}
import ProxyBwapi.Upgrades.{Upgrade, Upgrades}

final class MacroEvent {
  var dFrames: Int = _
  var dMinerals: Int = _
  var dGas: Int = _
  var dSupplyAvailable: Int = _
  var dSupplyUsed: Int = _
  var dMineralPatches: Int = _
  var dGeysers: Int = _
  var dTech: Tech = Techs.None
  var dUpgrade: Upgrade = Upgrades.None
  var dUpgradeLevel: Int = _
  var dUnit1: UnitClass = UnitClasses.None
  var dUnit1N: Int = _
  var dUnit2: UnitClass = UnitClasses.None
  var dUnit2N: Int = _
  var dProducer: UnitClass = _
  var dProducerN: Int = _
}
