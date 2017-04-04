package Planning.Composition.PositionFinders.Tactics

import Planning.Composition.PositionFinders.PositionFinder
import Lifecycle.With
import bwapi.TilePosition

class PositionEnemyBase extends PositionFinder {
  
  override def find: Option[TilePosition] = Some(With.intelligence.mostBaselikeEnemyPosition)
  
}
