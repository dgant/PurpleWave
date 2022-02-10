package Macro.MacroSim

import ProxyBwapi.Techs.{Tech, Techs}
import ProxyBwapi.UnitClasses.{UnitClass, UnitClasses}
import ProxyBwapi.Upgrades.{Upgrade, Upgrades}
import Utilities.Time.Frames

final class MacroEvent {
  var dFrames           : Int = 0
  var dMinerals         : Int = 0
  var dGas              : Int = 0
  var dSupplyAvailable  : Int = 0
  var dSupplyUsed       : Int = 0
  var dMineralPatches   : Int = 0
  var dGeysers          : Int = 0
  var dTech             : Tech      = Techs.None
  var dUpgrade          : Upgrade   = Upgrades.None
  var dUpgradeLevel     : Int = 0
  var dUnit1            : UnitClass = UnitClasses.None
  var dUnit1N           : Int = 0
  var dUnit2            : UnitClass = UnitClasses.None
  var dUnit2N           : Int = 0
  var dProducer1        : UnitClass = UnitClasses.None
  var dProducer1N       : Int = 0
  var dProducer2        : UnitClass = UnitClasses.None
  var dProducer2N       : Int = 0

  private def sign(value: Int): String = {
    if (value > 0) f"+$value" else f"$value"
  }
  private def free(value: Int): String = {
    if (value > 0) f"Avail $value" else f"Occupy ${-value}"
  }
  override def toString: String = {
    var output = f"${Frames(dFrames)} "
    if (dTech != Techs.None)            output += f"$dTech "
    if (dUpgrade != Upgrades.None)      output += f"$dUpgrade $dUpgradeLevel "
    if (dUnit1 != UnitClasses.None)     output += f"${sign(dUnit1N)} $dUnit1 "
    if (dUnit2 != UnitClasses.None)     output += f"${sign(dUnit2N)} $dUnit2 "
    if (dProducer1 != UnitClasses.None) output += f"${free(dProducer1N)} $dProducer1 "
    if (dProducer2 != UnitClasses.None) output += f"${free(dProducer2N)} $dProducer2 "
    if (dMinerals != 0)                 output += f"${sign(dMinerals)}m "
    if (dGas != 0)                      output += f"${sign(dGas)}g "
    if (dSupplyUsed != 0)               output += f"${sign(-dSupplyUsed)}s "
    if (dSupplyAvailable != 0)          output += f"${sign(dSupplyAvailable)}s "
    if (dMineralPatches != 0)           output += f"${sign(dMineralPatches)} patches "
    if (dGeysers != 0)                  output += f"${sign(dMineralPatches)} geysers "
    output
  }
}
