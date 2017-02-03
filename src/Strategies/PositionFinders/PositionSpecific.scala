package Strategies.PositionFinders
import bwapi.TilePosition

class PositionSpecific(
  val position:TilePosition)
  extends PositionFinder {
  
  override def find(): Option[TilePosition] = Some(position)
}
