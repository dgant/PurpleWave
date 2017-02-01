package Types.PositionFinders

import Development.Logger
import Processes.Architect
import bwapi.{TilePosition, UnitType}

class PositionFindBuildingSimple(
  val buildingType:UnitType)
    extends PositionFinder {
  
  override def find(): Option[TilePosition] = {
    val output = Architect.placeBuilding(
      buildingType,
      Architect.getHq,
      margin = 1)
    
    if (output == None) {
      Logger.warn("Failed to place a " ++ buildingType.toString)
    }
    
    output
  }
}
