package Traits

import Strategies.PositionFinders.{PositionCenter, PositionFinder}

trait TraitSettablePositionFinder {
  
  var _positionFinder:PositionFinder = new PositionCenter
  
  def getPositionFinder():PositionFinder = {
    _positionFinder
  }
  
  def setPositionFinder(positionFinder: PositionFinder) {
    _positionFinder = positionFinder
  }
}
