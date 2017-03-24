package Planning.Composition.PositionFinders
import Startup.With
import bwapi.TilePosition

class PositionEnemyBase extends PositionFinder {
  
  override def find: Option[TilePosition] = Some(With.intelligence.mostBaselikeEnemyPosition)
  
}
