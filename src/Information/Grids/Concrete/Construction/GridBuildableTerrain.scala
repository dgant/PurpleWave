package Information.Grids.Concrete.Construction

import Information.Grids.Abstract.ArrayTypes.GridBoolean
import Startup.With

class GridBuildableTerrain extends GridBoolean {
  override def onInitialization() {
    tiles.foreach(tile => set(tile, With.game.isBuildable(tile)))
  }
}
