package Mathematics.Points

import Lifecycle.With

object SpecificPoints {
  
  def middle = Pixel(
    With.mapWidth * 32 / 2,
    With.mapHeight * 32 / 2)
  
  def tileMiddle = Tile(
      With.mapWidth / 2,
      With.mapHeight / 2)
}
