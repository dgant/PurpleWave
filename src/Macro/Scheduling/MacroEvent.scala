package Macro.Scheduling

import Mathematics.Points.Tile
import ProxyBwapi.Techs.{Tech, Techs}
import ProxyBwapi.UnitClasses.{UnitClass, UnitClasses}
import ProxyBwapi.Upgrades.{Upgrade, Upgrades}
import Utilities.?
import Utilities.Time.Frames

final class MacroEvent(val state: MacroState) {
  var dFrames             : Int           = 0
  var dMinerals           : Int           = 0
  var dGas                : Int           = 0
  var dSupplyAvailable    : Int           = 0
  var dSupplyUsed         : Int           = 0
  var dMineralPatches     : Int           = 0
  var dGeysers            : Int           = 0
  var dTech               : Tech          = Techs.None
  var dUpgrade            : Upgrade       = Upgrades.None
  var dUpgradeLevel       : Int           = 0
  var dUnitExtant1        : UnitClass     = UnitClasses.None
  var dUnitExtant1N       : Int           = 0
  var dUnitExtant2        : UnitClass     = UnitClasses.None
  var dUnitExtant2N       : Int           = 0
  var dUnitComplete       : UnitClass     = UnitClasses.None
  var dUnitCompleteN      : Int           = 0
  var dUnitCompleteASAP   : UnitClass     = UnitClasses.None
  var dUnitCompleteASAPN  : Int           = 0
  var dProducer1          : UnitClass     = UnitClasses.None
  var dProducer1N         : Int           = 0
  var dProducer2          : UnitClass     = UnitClasses.None
  var dProducer2N         : Int           = 0
  var tile                : Option[Tile]  = None

  private def sign(value: Int): String = {
    ?(value >= 0, f"+$value", f"$value")
  }
  private def count(value: Int): String = {
    f"($value)"
  }
  private def freeOrDone(unitClass: UnitClass): String = {
    ?(dUnitComplete == unitClass, f"Done $unitClass", f"Free $unitClass")
  }
  private def tileString: String = {
    tile.map(t => f" $t").mkString("")
  }
  override def toString: String = {
    var output = f"${Frames(dFrames)} "
    if (dTech             != Techs.None)        output += f"$dTech "
    if (dUpgrade          != Upgrades.None)     output += f"$dUpgrade $dUpgradeLevel "
    if (dUnitExtant1      != UnitClasses.None)  output += f"${sign(dUnitExtant1N)} ${count(state.unitsExtant(dUnitExtant1))} $dUnitExtant1 "
    if (dUnitExtant2      != UnitClasses.None)  output += f"${sign(dUnitExtant2N)} ${count(state.unitsExtant(dUnitExtant2))} $dUnitExtant2 "
    if (dProducer1        != UnitClasses.None)  output += f"${sign(dProducer1N)} ${count(state.producers(dProducer1))} ${freeOrDone(dProducer1)} "
    if (dProducer2        != UnitClasses.None)  output += f"${sign(dProducer2N)} ${count(state.producers(dProducer2))} ${freeOrDone(dProducer2)} "
    if (dMinerals         != 0)                 output += f"${sign(dMinerals)}m "
    if (dGas              != 0)                 output += f"${sign(dGas)}g "
    if (dSupplyUsed       != 0)                 output += f"${sign(-dSupplyUsed)}s "
    if (dSupplyAvailable  != 0)                 output += f"${sign(dSupplyAvailable)}s "
    if (dMineralPatches   != 0)                 output += f"${sign(dMineralPatches)} patches "
    if (dGeysers          != 0)                 output += f"${sign(dMineralPatches)} geysers "
    output += tileString
    output
  }
}
