package Types.PositionFinders

import bwapi.TilePosition

trait PositionFinder {
  
  def find():TilePosition
  
}
