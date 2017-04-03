package Planning.Composition.PositionFinders.Generic

import Geometry.Positions
import Planning.Composition.PositionFinders.PositionFinder
import bwapi.TilePosition

object PositionMiddle extends PositionFinder {
  
  override def find(): Option[TilePosition] = Some(Positions.tileMiddle)
}
