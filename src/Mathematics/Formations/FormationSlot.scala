package Mathematics.Formations

import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class FormationSlot(unit: FriendlyUnitInfo) {
  val unitClass: UnitClass = unit.unitClass
  val rangePixels: Double = range(unit)
  val sightPixels: Int = unit.sightPixels
  
  private def range(unit: FriendlyUnitInfo): Double = {
    if (unit.is(Terran.SiegeTankUnsieged) && unit.player.hasTech(Terran.SiegeMode))
      Terran.SiegeTankSieged.effectiveRangePixels
    else if (unit.is(Terran.Medic))
      Terran.Marine.effectiveRangePixels - 32
    else
      unit.effectiveRangePixels
  }
}
