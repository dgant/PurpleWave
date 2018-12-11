package Mathematics.Formations

import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.UnitInfo

class FormationSlot(unit: UnitInfo) {
  val unitClass: UnitClass = unit.unitClass
  val idealPixels : Double = range(unit)
  
  private def range(unit: UnitInfo): Double = {
    if (unit.isSiegeTankUnsieged() && unit.player.hasTech(Terran.SiegeMode))
      Terran.SiegeTankSieged.effectiveRangePixels
    else
      unit.effectiveRangePixels
  }
}
