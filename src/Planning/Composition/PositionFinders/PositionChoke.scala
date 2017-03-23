package Planning.Composition.PositionFinders
import Performance.Caching.Cache
import Startup.With
import Utilities.EnrichPosition._
import bwapi.TilePosition

class PositionChoke extends PositionFinder {
  
  override def find: Option[TilePosition] = findCache.get
  val findCache = new Cache[Option[TilePosition]](3, () => findRecalculate)
  private def findRecalculate:Option[TilePosition] = {
    
    val home = With.geography.home
    val ourExposedChokes = With.geography.zones
      .filter(_.owner == With.self)
      .flatten(_.edges)
      .filter(edge => edge.zones.exists(_.owner != With.self))
    
    if (ourExposedChokes.isEmpty) return Some(home)
    
    val furthestStartPosition = With.geography.bases.filter(_.isStartLocation).map(_.tile).maxBy(home.distanceTileSquared)
    
    val mostExposedChoke = ourExposedChokes
      .minBy(choke => With.paths.groundPixels(
        choke.chokepoint.getCenter.toTilePosition,
        home))
    
    return Some(mostExposedChoke.chokepoint.getCenter.toTilePosition)
  }
}
