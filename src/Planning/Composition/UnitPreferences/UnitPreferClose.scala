package Planning.Composition.UnitPreferences

import Planning.Composition.PixelFinders.Generic.TileMiddle
import Planning.Composition.PixelFinders.TileFinder
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Planning.Composition.Property
import Utilities.EnrichPixel._

class UnitPreferClose extends UnitPreference {
  
  val positionFinder = new Property[TileFinder](TileMiddle)
  
  override def preference(unit: FriendlyUnitInfo): Double = {
    positionFinder.get.find
      .map(tile => tile.pixelCenter.pixelDistanceFast(unit.pixelCenter))
      .getOrElse(0)
  }
}
