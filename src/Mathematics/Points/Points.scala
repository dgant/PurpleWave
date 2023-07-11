package Mathematics.Points

import Lifecycle.With
import Mathematics.Maff

object Points {
  
  def middle: Pixel = Pixel(
    Maff.div2(With.mapPixelWidth),
    Maff.div2(With.mapPixelHeight))
  
  def tileMiddle: Tile = Tile(
    Maff.div2(With.mapTileWidth),
    Maff.div2(With.mapTileHeight))
}
