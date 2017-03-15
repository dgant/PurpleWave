package Planning.Composition.PositionFinders

import bwapi.TilePosition

trait PositionFinder {
  
  def find:Option[TilePosition]
  
}
