package Planning.Composition.PositionFinders
import Geometry.Positions
import Startup.With
import Performance.Caching.Cache
import Utilities.TypeEnrichment.EnrichPosition._
import bwapi.TilePosition

class PositionChoke extends PositionFinder {
  
  override def find: Option[TilePosition] = findCache.get
  val findCache = new Cache[Option[TilePosition]](1, () => findRecalculate)
  private def findRecalculate:Option[TilePosition] = {
    
    val home = With.geography.home
    val chokes = With.geography.ourBases.flatten(_.zone.edges.map(_.chokepoint)).map(_.getCenter.toTilePosition)
    
    if (chokes.isEmpty) return Some(home)
    
    val bases =
      if (With.geography.ourBases.nonEmpty) With.geography.ourBases.map(_.tile)
      else List(home)
    
    val furthestStartPosition = With.geography.bases.filter(_.isStartLocation).map(_.tile).maxBy(home.distanceTile)
    val possibleChokes = chokes.filter(choke =>
      bases.forall(base =>
        With.paths.groundDistance(furthestStartPosition, choke) <
        With.paths.groundDistance(furthestStartPosition, base)))
    
    if (possibleChokes.isEmpty) return Some(bases.minBy(Positions.tileMiddle.distanceTile))
    
    Some(possibleChokes.minBy(home.distanceTile))
  }
}
