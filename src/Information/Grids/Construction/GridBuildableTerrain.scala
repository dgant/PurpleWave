package Information.Grids.Construction

import Information.Grids.ArrayTypes.AbstractGridBoolean
import Lifecycle.With

class GridBuildableTerrain extends AbstractGridBoolean {
  override def onInitialization() {
    tiles.foreach(tile => set(tile, With.game.isBuildable(tile.bwapi)))
  }
}
