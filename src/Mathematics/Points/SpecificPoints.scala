package Mathematics.Points

import Lifecycle.With

object SpecificPoints {
  
  def middle: Pixel = Pixel(
    With.mapPixelWidth / 2,
    With.mapPixelHeight / 2)
  
  def tileMiddle: Tile = Tile(
    With.mapTileWidth / 2,
    With.mapTileHeight / 2)
}
