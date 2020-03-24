package Information.Grids.Construction

import Information.Grids.ArrayTypes.AbstractGridBoolean
import Lifecycle.With
import Mathematics.Points.Tile

class GridCreepInitial extends AbstractGridBoolean {
  
  override def onInitialization() {
    indices.foreach(i => set(i, With.game.hasCreep(new Tile(i).bwapi)))
  }
}
