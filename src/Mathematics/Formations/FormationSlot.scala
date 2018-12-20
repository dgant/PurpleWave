package Mathematics.Formations

import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class FormationSlot(unit: FriendlyUnitInfo) {
  val unitClass: UnitClass = unit.unitClass
  val idealPixels : Double = range(unit)
  
  private def range(unit: FriendlyUnitInfo): Double = {
    if (unit.isSiegeTankUnsieged() && unit.player.hasTech(Terran.SiegeMode))
      Terran.SiegeTankSieged.effectiveRangePixels
    else
      (unit.loadedUnits.map(_.effectiveRangePixels) ++ Seq(unit.effectiveRangePixels)).max
  }
}
