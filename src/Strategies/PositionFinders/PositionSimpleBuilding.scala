package Strategies.PositionFinders

import Development.Logger
import Startup.With
import bwapi.{TilePosition, UnitType}

class PositionSimpleBuilding(
  val buildingType:UnitType)
    extends PositionFinder {
  
  override def find(): Option[TilePosition] = {
    val position = With.architect.getHq
    val output = With.architect.placeBuilding(
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
