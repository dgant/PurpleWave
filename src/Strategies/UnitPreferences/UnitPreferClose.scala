package Strategies.UnitPreferences

import Strategies.PositionFinders.{PositionCenter, PositionFinder}
import Types.UnitInfo.FriendlyUnitInfo
import Utilities.Property
import Utilities.Enrichment.EnrichPosition._

class UnitPreferClose extends UnitPreference {
  
  val positionFinder = new Property[PositionFinder](new PositionCenter)
  
  override def preference(unit: FriendlyUnitInfo): Double = {
    positionFinder.get.find
      .map(position => position.centerPosition.getDistance(unit.position))
      .getOrElse(0)
  }
}
