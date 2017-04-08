package Planning.Composition.PositionFinders.Tactics

import Planning.Composition.PositionFinders.TileFinder
import Lifecycle.With
import bwapi.TilePosition

object TileHome$ extends TileFinder {
  
  override def find(): Option[TilePosition] = Some(With.geography.home)
}
