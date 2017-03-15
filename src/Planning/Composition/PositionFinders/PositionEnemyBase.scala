package Planning.Composition.PositionFinders
import Startup.With
import Performance.Caching.Cache
import bwapi.TilePosition

class PositionEnemyBase extends PositionFinder {
  
  override def find: Option[TilePosition] = findCache.get
  
  val findCache = new Cache[Option[TilePosition]](3, () => findRecalculate)
  
  private def findRecalculate: Option[TilePosition] = Some(
    With.intelligence.mostBaselikeEnemyBuilding.map(_.tileCenter).getOrElse(
      With.intelligence.leastScoutedBases.head))
}
