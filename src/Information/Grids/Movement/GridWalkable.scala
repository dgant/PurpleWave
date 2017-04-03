package Information.Grids.Movement

import Information.Grids.AbstractGrid
import Startup.With

class GridWalkable extends AbstractGrid[Boolean] {
  def get(i:Int) = With.grids.walkableTerrain.get(i) && With.grids.walkableUnits.get(i)
}
