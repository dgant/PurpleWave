package Planning.Composition.PositionFinders

import bwapi.TilePosition

trait TileFinder {
  
  def find:Option[TilePosition]
  
}
