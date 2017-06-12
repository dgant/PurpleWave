package Planning.Composition.PixelFinders.Tactics

import Lifecycle.With
import Mathematics.Pixels.Tile
import Performance.Caching.Cache
import Planning.Composition.PixelFinders.TileFinder

class TileChoke extends TileFinder {
  
  override def find: Option[Tile] = findCache.get
  val findCache = new Cache(3, () => findRecalculate)
  
  private def findRecalculate: Option[Tile] = {
    
    val ourExposedChokes = With.geography.zones
      .filter(zone =>
        zone.owner == With.self ||
        With.executor.states.exists(state =>
          state.intent.toBuild.nonEmpty &&
          state.intent.toTravel.exists(_.zone == zone)))
      .flatten(_.edges)
      .filter(edge => edge.zones.exists(_.owner != With.self))
    
    if (ourExposedChokes.isEmpty) return Some(With.geography.home)
    
    val mostExposedChoke = ourExposedChokes.minBy(choke =>
      With.paths.groundPixels(
        choke.centerPixel.tileIncluding,
        With.intelligence.mostBaselikeEnemyTile))
    
    return Some(mostExposedChoke.centerPixel.tileIncluding)
  }
}
