package Processes

import Startup.With
import bwapi.{TilePosition, UnitType}

object Architect {
  
  def getHq():TilePosition = {
    With.ourUnits
      .filter(_.getType.isResourceDepot)
      .map(_.getTilePosition)
      .headOption
      .getOrElse(new TilePosition(0, 0))
  }
  
  def placeBuilding(
    buildingType:UnitType,
    center:TilePosition,
    margin:Integer = 0,
    searchRadius:Integer = 20)
      :Option[TilePosition] = {
  
    _radialSearch(center, searchRadius)
      .filter(_canBuildWithMargin(_, buildingType, margin))
      .headOption
  }
  
  def _radialSearch(
    position:TilePosition,
    searchRadius:Integer=20)
      :Iterable[TilePosition] = {
    for (
      dx <- 0 to searchRadius;
      dy <- 0 to searchRadius;
      mx <- List(-1, 1);
      my <- List(-1, 1))
      yield new TilePosition(
        position.getX + mx * dx,
        position.getY + my * dy)
  }
  
  def _canBuildWithMargin(
    position:TilePosition,
    buildingType:UnitType,
    margin:Integer):Boolean = {
    (Iterable(position) ++ _radialSearch(position, margin))
        .forall(With.game.canBuildHere(_, buildingType))
  }
}
