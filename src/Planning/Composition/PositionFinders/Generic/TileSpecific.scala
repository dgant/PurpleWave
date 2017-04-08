package Planning.Composition.PositionFinders.Generic

import Planning.Composition.PositionFinders.TileFinder
import bwapi.TilePosition

case class TileSpecific(val position:TilePosition) extends TileFinder {
  override def find(): Option[TilePosition] = Some(position)
}
