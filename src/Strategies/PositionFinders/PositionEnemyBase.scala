package Strategies.PositionFinders
import Startup.With
import Utilities.Caching.Cache
import bwapi.TilePosition

class PositionEnemyBase extends PositionFinder {
  
  val _cache = new Cache[Option[TilePosition]](24 * 3, () => _find)
  
  override def find: Option[TilePosition] = _cache.get
  
  def _find: Option[TilePosition] = Some(
    With.intelligence.mostBaselikeEnemyBuilding.map(_.tileCenter).getOrElse(
      With.intelligence.leastScoutedBases.head))
}
