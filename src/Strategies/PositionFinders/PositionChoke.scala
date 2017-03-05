package Strategies.PositionFinders
import Geometry.Positions
import Startup.With
import Utilities.Caching.Cache
import bwapi.TilePosition
import Utilities.Enrichment.EnrichPosition._

object PositionChoke extends PositionFinder {
  
  val _cachedChoke = new Cache[Option[TilePosition]](24, () => _recalculate)
  
  override def find: Option[TilePosition] = _cachedChoke.get
  
  def _recalculate:Option[TilePosition] = {
    if (With.geography.chokes.isEmpty || With.geography.basePositions.isEmpty) return Some(With.geography.home)
    Some(With.geography.chokes.map(_.toTilePosition).minBy(With.geography.ourBases.minBy(Positions.tileMiddle.distance).distance))
  }
}
