package Information.Grids.Construction

import Information.Grids.AbstractTypedGrid
import Lifecycle.With

final class GridBuildable extends AbstractTypedGrid[Boolean] {

  @inline def getUnchecked(i: Int): Boolean =  With.grids.buildableTerrain.getUnchecked(i) && ! With.grids.unwalkableUnits.isSetUnchecked(i)
  
  override val defaultValue: Boolean = false
}
