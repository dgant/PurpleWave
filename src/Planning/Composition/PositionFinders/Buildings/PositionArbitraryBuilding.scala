package Planning.Composition.PositionFinders.Buildings

import Planning.Composition.PositionFinders.PositionFinder
import ProxyBwapi.UnitClass.UnitClass
import bwapi.TilePosition

class PositionArbitraryBuilding(val buildingClass:UnitClass) extends PositionFinder {
  
  val positionSimpleBuilding = new PositionSimpleBuilding(buildingClass)
  
  def find: Option[TilePosition] = {
    if      (buildingClass.isRefinery)  return PositionRefinery.find
    else if (buildingClass.isTownHall)  return PositionTownHall.find
    else                                return positionSimpleBuilding.find
  }
}
