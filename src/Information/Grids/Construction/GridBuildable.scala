package Information.Grids.Construction

import Information.Grids.AbstractGrid
import Lifecycle.With

class GridBuildable extends AbstractGrid[Boolean] {

  @inline final def getUnchecked(i: Int): Boolean =  With.grids.buildableTerrain.getUnchecked(i) && ! With.grids.unwalkableUnits.isSetUnchecked(i)
  
  final override val defaultValue: Boolean = false
}
