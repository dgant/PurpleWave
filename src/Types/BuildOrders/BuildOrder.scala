package Types.BuildOrders

import bwapi.UnitType

class BuildOrder {
  def buildOrder = Array(
    UnitType.Terran_SCV,
    UnitType.Terran_SCV,
    UnitType.Terran_SCV,
    UnitType.Terran_SCV,
    UnitType.Terran_SCV,
    UnitType.Terran_SCV,
    UnitType.Terran_Barracks,
    UnitType.Terran_Supply_Depot,
    UnitType.Terran_Marine
  )

  def getUnitTypes():Iterable[bwapi.UnitType] = {
    return buildOrder
  }
}
