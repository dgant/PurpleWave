package Planning.Composition.PositionFinders
import Geometry.Positions
import bwapi.TilePosition

object PositionCenter extends PositionFinder {
  
  override def find(): Option[TilePosition] = Some(Positions.tileMiddle)
}
