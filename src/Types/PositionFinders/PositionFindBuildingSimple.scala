package Types.PositionFinders

import Processes.Architect
import bwapi.{TilePosition, UnitType}

class PositionFindBuildingSimple(
  val buildingType:UnitType)
    extends PositionFinder {
  
  override def find(): Option[TilePosition] = {
    Architect.placeBuilding(
      buildingType,
      Architect.getHq,
      margin = 1)
  }
}
