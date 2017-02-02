package Types.PositionFinders

import Development.Logger
import Processes.Architect
import bwapi.{TilePosition, UnitType}

class PositionSimpleBuilding(
  val buildingType:UnitType)
    extends PositionFinder {
  
  override def find(): Option[TilePosition] = {
    val position = Architect.getHq
    val output = Architect.placeBuilding(
      buildingType,
      position,
      margin = 0,
      searchRadius = 50)
    
    if (output == None) {
      Logger.warn("Failed to place a " ++ buildingType.toString ++ " near " ++ position.toString)
    }
    
    output
  }
}
