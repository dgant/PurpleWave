package Planning.Composition.PositionFinders.Tactics

import Performance.Caching.Cache
import Planning.Composition.PositionFinders.TileFinder
import Lifecycle.With
import Utilities.EnrichPosition._
import bwapi.TilePosition

class TileChoke extends TileFinder {
  
  override def find: Option[TilePosition] = findCache.get
  val findCache = new Cache[Option[TilePosition]](3, () => findRecalculate)
  private def findRecalculate:Option[TilePosition] = {
    
    val home = With.geography.home
    val ourExposedChokes = With.geography.zones
      .filter(zone =>
        zone.owner == With.self ||
        With.executor.states.exists(state =>
          state.intent.toBuild.nonEmpty &&
          state.intent.destination.exists(_.zone == zone)))
      .flatten(_.edges)
      .filter(edge => edge.zones.exists(_.owner != With.self))
    
    if (ourExposedChokes.isEmpty) return Some(home)
    
    val mostExposedChoke = ourExposedChokes.minBy(choke =>
      With.paths.groundPixels(
        choke.centerPixel.toTilePosition,
        With.intelligence.mostBaselikeEnemyPosition))
    
    return Some(mostExposedChoke.centerPixel.toTilePosition)
  }
}
