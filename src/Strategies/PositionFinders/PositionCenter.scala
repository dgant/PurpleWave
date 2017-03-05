package Strategies.PositionFinders
import Geometry.Positions
import bwapi.TilePosition

class PositionCenter extends PositionFinder {
  
  override def find(): Option[TilePosition] = Some(Positions.tileMiddle)
}
