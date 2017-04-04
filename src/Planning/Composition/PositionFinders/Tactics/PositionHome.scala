package Planning.Composition.PositionFinders.Tactics

import Planning.Composition.PositionFinders.PositionFinder
import Lifecycle.With
import bwapi.TilePosition

object PositionHome extends PositionFinder {
  
  override def find(): Option[TilePosition] = Some(With.geography.home)
}
