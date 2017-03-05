package Strategies.PositionFinders
import Geometry.Positions
import Startup.With
import Utilities.Caching.Cache
import Utilities.Enrichment.EnrichPosition._
import bwapi.TilePosition

class PositionChoke extends PositionFinder {
  
  val _cachedChoke = new Cache[Option[TilePosition]](24, () => _recalculate)
  
  override def find: Option[TilePosition] = _cachedChoke.get
  
  def _recalculate:Option[TilePosition] = {
    
    val home = With.geography.home
    val chokes = With.geography.chokes.map(_.toTilePosition)
    
    if (chokes.isEmpty) return Some(home)
    
    val bases = if (With.geography.ourBases.nonEmpty) With.geography.ourBases else List(home)
    val furthestStartPosition = With.geography.startPositions.toList.maxBy(home.distance)
    val possibleChokes = chokes.filter(choke =>
      bases.forall(base =>
        With.paths.groundDistance(furthestStartPosition, choke) <
        With.paths.groundDistance(furthestStartPosition, base)))
    
    if (possibleChokes.isEmpty) return Some(bases.minBy(Positions.tileMiddle.distance))
    
    Some(possibleChokes.minBy(home.distance))
  }
}
