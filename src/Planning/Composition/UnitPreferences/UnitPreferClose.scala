package Planning.Composition.UnitPreferences

import Planning.Composition.PositionFinders.Generic.TileMiddle
import Planning.Composition.PositionFinders.TileFinder
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Planning.Composition.Property
import Utilities.EnrichPosition._

class UnitPreferClose extends UnitPreference {
  
  val positionFinder = new Property[TileFinder](TileMiddle)
  
  override def preference(unit: FriendlyUnitInfo): Double = {
    positionFinder.get.find
      .map(tile => tile.pixelCenter.getDistance(unit.pixelCenter))
      .getOrElse(0)
  }
}
