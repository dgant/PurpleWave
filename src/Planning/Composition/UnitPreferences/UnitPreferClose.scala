package Planning.Composition.UnitPreferences

import Planning.Composition.PositionFinders.{PositionCenter, PositionFinder}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Planning.Composition.Property
import Utilities.TypeEnrichment.EnrichPosition._

class UnitPreferClose extends UnitPreference {
  
  val positionFinder = new Property[PositionFinder](new PositionCenter)
  
  override def preference(unit: FriendlyUnitInfo): Double = {
    positionFinder.get.find
      .map(position => position.centerPixel.getDistance(unit.pixel))
      .getOrElse(0)
  }
}
