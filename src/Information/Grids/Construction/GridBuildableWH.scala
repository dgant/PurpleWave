package Information.Grids.Construction

import Information.Grids.ArrayTypes.AbstractGridArrayBoolean
import Lifecycle.With

final class GridBuildableWH(w: Int, h: Int) extends AbstractGridArrayBoolean {

  override val defaultValue: Boolean = true

  override def onInitialization(): Unit = {
    reset()
    var x, y, clearance = 0

    // Verify horizontal clearance
    y = With.mapTileHeight - 1
    while (y >= 0) {
      x = With.mapTileWidth - 1
      while (x >= 0) {
        if (With.game.isBuildable(x, y)) clearance += 1 else clearance = 0
        if (clearance < w) {
          set(x, y, false)
        }
        x -= 1
      }
      clearance = 0
      y -= 1
    }

    // Verify vertical clearance
    x =  With.mapTileWidth - 1
    while (x >= 0) {
      y = With.mapTileHeight - 1
      while (y >= 0) {
        if (With.game.isBuildable(x, y)) clearance += 1 else clearance = 0
        if (clearance < h) {
          set(x, y, false)
        }
        y -= 1
      }
      clearance = 0
      x -= 1
    }
  }
}
