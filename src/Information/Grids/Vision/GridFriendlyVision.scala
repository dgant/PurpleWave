package Information.Grids.Vision

import Information.Grids.ArrayTypes.AbstractGridTimestamp
import Lifecycle.With
import Mathematics.Points.Tile

class GridFriendlyVision extends AbstractGridTimestamp {
  
  override protected def updateTimestamps() {
    var index = 0
    while(index < length) {
      val tile = new Tile(index)
      if (With.game.isVisible(tile.x, tile.y))
      set(index, frameUpdated)
      index += 1
    }
  }
}