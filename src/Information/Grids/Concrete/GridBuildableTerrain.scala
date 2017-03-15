package Information.Grids.Concrete

import Information.Grids.Abstract.GridBoolean
import Startup.With

class GridBuildableTerrain extends GridBoolean {
  override def onInitialization() {
    positions.foreach(tilePosition => set(tilePosition, With.game.isBuildable(tilePosition)))
  }
}
