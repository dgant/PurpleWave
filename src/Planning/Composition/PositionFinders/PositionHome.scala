package Planning.Composition.PositionFinders

import Startup.With
import bwapi.TilePosition

object PositionHome extends PositionFinder {
  
  override def find(): Option[TilePosition] = Some(With.geography.home)
}
