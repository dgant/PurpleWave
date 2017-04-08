package Planning.Composition.PositionFinders.Tactics

import Planning.Composition.PositionFinders.TileFinder
import Lifecycle.With
import bwapi.TilePosition

class TileEnemyBase extends TileFinder {
  
  override def find: Option[TilePosition] = Some(With.intelligence.mostBaselikeEnemyPosition)
  
}
