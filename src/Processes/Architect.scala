package Processes

import Startup.With
import bwapi.{TilePosition, UnitType}

class Architect {
  
  def getHq:TilePosition = {
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
  //Via http://stackoverflow.com/questions/3706219/algorithm-for-iterating-over-an-outward-spiral-on-a-discrete-2d-grid-from-the-or
  
  def _radialSearch(
    position:TilePosition,
    searchRadius:Integer = 20)
      :Iterable[TilePosition] = {
    var dx = 1
    var dy = 0
    var segment_length = 1
    
    var x = position.getX
    var y = position.getY
    var segment_passed = 0
    
    val pointsToSearch = (2 * searchRadius + 1) * (2 * searchRadius + 1)
    (0 to pointsToSearch)
      .map(i => {
        x += dx
        y += dy
        segment_passed += 1
  
        if (segment_passed == segment_length) {
          segment_passed = 0
          val swap = dx
          dx = -dy
          dy = swap
          if (dy == 0) {
            segment_length += 1
          }
        }
        new TilePosition(x, y)
      })
  }
  
  def _canBuildWithMargin(
    position:TilePosition,
    buildingType:UnitType,
    margin:Integer):Boolean = {
  
    (-margin to margin).forall(x =>
      (-margin to margin).forall(y =>
        _test(
          new TilePosition(
            position.getX + x,
            position.getY + y),
          buildingType)))
  }
  
  def _test(position: TilePosition, buildingType:UnitType):Boolean = {
    //This tends to get confused due to the builder being nearby
    val likelyBuilders = With.ourUnits
      .filter(_.getType.isWorker)
      .filter(_.getDistance(position.toPosition) < 32 * 3)
      .take(3)
    
    if (likelyBuilders.nonEmpty) {
      likelyBuilders.exists(builder => With.game.canBuildHere(position, buildingType, builder))
    }
    else {
      With.game.canBuildHere(position, buildingType)
    }
  }
}
