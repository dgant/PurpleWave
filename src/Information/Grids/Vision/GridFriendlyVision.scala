package Information.Grids.Vision

import Information.Grids.ArrayTypes.AbstractGridFramestamp
import Lifecycle.With
import Mathematics.Points.Tile

class GridFriendlyVision extends AbstractGridFramestamp {

  def hasSeen(tile: Tile): Boolean = get(tile) > 0
  def hasSeen(i: Int): Boolean = get(i) > 0
  
  override protected def updateTimestamps() {
    var x = 0
    while (x < With.mapTileWidth) {
      var y = 0
      while (y < With.mapTileHeight) {
        if (With.game.isVisible(x, y)) {
          stamp(x, y)
        }
        y += 1
      }
      x += 1
    }
  }
}