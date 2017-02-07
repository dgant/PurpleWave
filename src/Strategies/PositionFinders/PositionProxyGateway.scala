package Strategies.PositionFinders

import Caching.Cache
import Development.Logger
import Startup.With
import Traits.Property
import bwapi.{TilePosition, UnitType}

class PositionProxyGateway extends PositionFinder {
  val proxyFinder = new Property[PositionFinder](PositionProxyArea)
  val cache = new Cache[Option[TilePosition]] { override def recalculate(): Option[TilePosition] = _recalculate}
  override def find(): Option[TilePosition] = { cache.get }
  
  def _recalculate:Option[TilePosition] = {
      proxyFinder.get.find
        .map(proxyTilePosition =>
          With.ourUnits
            .filter(_.getType == UnitType.Protoss_Pylon)
            .sortBy(_.getDistance(proxyTilePosition.toPosition))
            .map(_.getTilePosition)
            .headOption
            .getOrElse({
              Logger.warn("Can't position proxy gateway because we failed to find a Pylon")
              proxyTilePosition
            }))
        .map(pylonTilePosition =>
          With.architect.placeBuilding(
            UnitType.Protoss_Gateway,
            pylonTilePosition,
            0)
            .getOrElse({
              Logger.warn("Failed to position proxy Gateway")
              pylonTilePosition
            }))
  }
}
