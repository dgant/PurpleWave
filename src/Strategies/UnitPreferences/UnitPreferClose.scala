package Strategies.UnitPreferences

import Strategies.PositionFinders.{PositionCenter, PositionFinder}
import Types.Property
import bwapi.Unit

class UnitPreferClose extends UnitPreference {
  
  val positionFinder = new Property[PositionFinder](new PositionCenter)
  
  override def preference(unit: Unit): Double = {
    positionFinder.get.find
      .map(position => position.toPosition.getDistance(unit.getPosition))
      .getOrElse(0)
  }
}
