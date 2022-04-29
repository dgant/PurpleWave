package Information.Grids.Construction

import Information.Grids.ArrayTypes.AbstractGridArrayBoolean
import Lifecycle.With

final class GridBuildableTerrain extends AbstractGridArrayBoolean {
  
  override def onInitialization(): Unit = {
    indices.foreach(i => set(i, With.game.isBuildable(i % With.mapTileWidth, i / With.mapTileWidth)))
  }
}

