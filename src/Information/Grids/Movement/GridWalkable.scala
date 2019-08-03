package Information.Grids.Movement

import Information.Grids.AbstractGrid
import Lifecycle.With

class GridWalkable extends AbstractGrid[Boolean] {

  @inline
  final override def get(i: Int): Boolean = With.grids.walkableTerrain.get(i) && ! With.grids.unwalkableUnits.isSet(i)
  
  @inline
  final override def defaultValue: Boolean = false
}
