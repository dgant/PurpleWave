package Planning.Composition.PositionFinders.Generic

import Mathematics.Positions.Positions
import Planning.Composition.PositionFinders.TileFinder
import bwapi.TilePosition

object TileMiddle extends TileFinder {
  
  override def find(): Option[TilePosition] = Some(Positions.tileMiddle)
}
