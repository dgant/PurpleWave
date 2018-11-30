package Information.Grids.Vision

import Information.Grids.ArrayTypes.AbstractGridTimestamp
import Lifecycle.With

class GridFriendlyVision extends AbstractGridTimestamp {
  
  override protected def updateTimestamps() {
    for (tile <- tiles) {
      if (With.game.isVisible(tile.x, tile.y)) {
        stamp(tile)
      }
    }
  }
}