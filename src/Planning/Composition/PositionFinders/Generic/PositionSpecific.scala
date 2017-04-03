package Planning.Composition.PositionFinders.Generic

import Planning.Composition.PositionFinders.PositionFinder
import bwapi.TilePosition

case class PositionSpecific(val position:TilePosition) extends PositionFinder {
  override def find(): Option[TilePosition] = Some(position)
}
