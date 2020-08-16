package Information.Grids.Construction

import Information.Grids.AbstractGrid
import Lifecycle.With

class GridBuildable extends AbstractGrid[Boolean] {

  @inline
  final override def get(i: Int): Boolean =  With.grids.buildableTerrain.get(i) && ! With.grids.unwalkableUnits.isSet(i)

  @inline
  final def getUnchecked(i: Int): Boolean =  With.grids.buildableTerrain.getUnchecked(i) && ! With.grids.unwalkableUnits.isSetUnchecked(i)
  
  override def defaultValue: Boolean = false
}
