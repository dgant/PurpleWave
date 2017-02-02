package Types.BuildOrders

import bwapi.UnitType

class BuildOrder {
  def _buildOrder = Array(
    UnitType.Terran_SCV,
    UnitType.Terran_SCV,
    UnitType.Terran_SCV,
    UnitType.Terran_SCV,
    UnitType.Terran_SCV,
    UnitType.Terran_SCV,
    UnitType.Terran_Barracks,
    UnitType.Terran_Supply_Depot,
    UnitType.Terran_Barracks,
    UnitType.Terran_Marine,
    UnitType.Terran_Marine,
    UnitType.Terran_SCV,
    UnitType.Terran_Supply_Depot,
    UnitType.Terran_Marine,
    UnitType.Terran_Marine,
    UnitType.Terran_SCV,
    UnitType.Terran_Marine,
    UnitType.Terran_SCV,
    UnitType.Terran_Supply_Depot,
    UnitType.Terran_Marine,
    UnitType.Terran_Marine,
    UnitType.Terran_SCV,
    UnitType.Terran_Marine,
    UnitType.Terran_SCV,
    UnitType.Terran_Barracks,
    UnitType.Terran_Barracks,
    UnitType.Terran_Barracks,
    UnitType.Terran_Barracks,
    UnitType.Terran_Barracks,
    UnitType.Terran_Barracks,
    UnitType.Terran_Barracks,
    UnitType.Terran_Barracks
  )

  def orders():Iterable[bwapi.UnitType] = {
    return _buildOrder
  }
}
