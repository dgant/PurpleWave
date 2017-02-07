package Strategies.PositionFinders

import Caching.Cache
import Startup.With
import Traits.Property
import bwapi.{TilePosition, UnitType}

object PositionProxyArea extends PositionFinder {
  
  val buildings = new Property[List[UnitType]](List.empty)
  
  val _cache = new Cache[TilePosition] {
    duration = 24 * 60
    recalculate
  }
  override def find: Option[TilePosition] = {
    Some(_cache.get)
  }
  
  def _recalculate: TilePosition = {
    val startingPoint =
      Some(new TilePosition(
        With.game.mapWidth / 2,
        With.game.mapHeight / 2))
    
    With.architect
  }
}
