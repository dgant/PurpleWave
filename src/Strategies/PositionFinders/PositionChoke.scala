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
    if (With.geography.chokes.isEmpty || With.geography.basePositions.isEmpty)
      return Some(With.geography.home)
    else
      return Some(With.geography.chokes
        .map(_.toTilePosition)
        .minBy(_.distance(
          if (With.geography.ourBases.nonEmpty) {
            With.geography.ourBases.minBy(Positions.tileMiddle.distance)
          } else {
           Positions.tileMiddle
          })))
  }
}
