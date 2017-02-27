package Strategies.UnitPreferences

import Strategies.PositionFinders.{PositionCenter, PositionFinder}
import Types.UnitInfo.FriendlyUnitInfo
import Utilities.Property

class UnitPreferClose extends UnitPreference {
  
  val positionFinder = new Property[PositionFinder](new PositionCenter)
  
  override def preference(unit: FriendlyUnitInfo): Double = {
    positionFinder.get.find
      .map(position => position.toPosition.getDistance(unit.position))
      .getOrElse(0)
  }
}
