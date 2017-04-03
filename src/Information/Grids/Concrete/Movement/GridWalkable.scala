package Information.Grids.Concrete.Movement

import Information.Grids.Abstract.Grid
import Startup.With

class GridWalkable extends Grid[Boolean] {
  def get(i:Int) = With.grids.walkableTerrain.get(i) && With.grids.walkableUnits.get(i)
}
