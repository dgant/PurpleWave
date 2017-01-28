package Processes

import Startup.With
import bwapi.{TilePosition, UnitType}

object Architect {
  def placeBuilding(
    buildingType:UnitType,
    approximatePosition:TilePosition)
      :Option[TilePosition] = {
    
    for (dx <- 0 to 20; dy <- 0 to 20; mx <- List(-1, 1); my <- List(-1, 1)) {
        var position = new TilePosition(
          approximatePosition.getX + mx * dx,
          approximatePosition.getY + my * dy)
      
        if (With.game.canBuildHere(position, buildingType)) {
          return Some(position)
        }
    }
    
    None
  }
}
