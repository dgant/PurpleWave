package Planning.Composition.PositionFinders.Generic

import Mathematics.Positions.Positions
import Planning.Composition.PositionFinders.PositionFinder
import bwapi.TilePosition

object PositionMiddle extends PositionFinder {
  
  override def find(): Option[TilePosition] = Some(Positions.tileMiddle)
}
