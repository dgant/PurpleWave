package Information.Grids.Movement

import Information.Grids.AbstractTypedGrid
import Lifecycle.With

class GridWalkable extends AbstractTypedGrid[Boolean] {
  @inline final def getUnchecked(i: Int): Boolean = With.grids.walkableTerrain.getUnchecked(i) && ! With.grids.unwalkableUnits.isSetUnchecked(i)
  final override val defaultValue: Boolean = false
}
