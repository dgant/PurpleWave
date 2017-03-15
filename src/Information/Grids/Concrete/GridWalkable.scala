package Information.Grids.Concrete

import Information.Grids.Abstract.Grid
import Startup.With

class GridWalkable extends Grid[Boolean] {
  def get(i:Int) = With.grids.walkableTerran.get(i) && With.grids.walkableUnits.get(i)
}
