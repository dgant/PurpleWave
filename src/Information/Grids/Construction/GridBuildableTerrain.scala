package Information.Grids.Construction

import Information.Grids.ArrayTypes.AbstractGridArrayBoolean
import Lifecycle.With
import Mathematics.Points.Tile

final class GridBuildableTerrain extends AbstractGridArrayBoolean {
  
  override def onInitialization() {
    indices.foreach(i => set(i, With.game.isBuildable(new Tile(i).bwapi)))
  }
}

