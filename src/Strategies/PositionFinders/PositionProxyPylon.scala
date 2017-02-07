package Strategies.PositionFinders

import Caching.Cache
import Startup.With
import Traits.Property
import bwapi.{TilePosition, UnitType}

class PositionProxyPylon extends PositionFinder {
  
  val searchRadius = new Property[Integer](40)
  
  val me = this
  val cache = new Cache[Option[TilePosition]] { override def recalculate: Option[TilePosition] = me._recalculate }
  
  override def find: Option[TilePosition] = {
    cache.get
  }
  
  def _recalculate: Option[TilePosition] = {
    With.architect.placeBuilding(
      UnitType.Protoss_Pylon,
      PositionProxyArea.find.get,
      margin = 3,
      searchRadius = searchRadius.get)
  }
}
