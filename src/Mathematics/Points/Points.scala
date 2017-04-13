package Mathematics.Pixels

import Lifecycle.With

object Points {
  def middle = new Pixel(
    With.mapWidth * 32 / 2,
    With.mapHeight * 32 / 2)
  
  def tileMiddle = new Tile(
      With.mapWidth / 2,
      With.mapHeight / 2)
}
