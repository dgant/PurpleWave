package Strategies.PositionFinders

import bwapi.TilePosition

trait PositionFinder {
  
  def find:Option[TilePosition]
  
}
