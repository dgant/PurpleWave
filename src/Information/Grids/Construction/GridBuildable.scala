package Information.Grids.Construction

import Information.Grids.AbstractGrid
import Lifecycle.With

class GridBuildable extends AbstractGrid[Boolean] {
  
  override def get(i: Int): Boolean =  With.grids.buildableTerrain.get(i) && ! With.grids.unwalkableUnits.isSet(i)
  
  override def defaultValue: Boolean = false
}
