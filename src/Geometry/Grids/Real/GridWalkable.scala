package Geometry.Grids.Real

import Geometry.Grids.Abstract.Grid
import Startup.With

class GridWalkable extends Grid[Boolean] {
  def get(i:Int) = With.grids.walkableTerran.get(i) && With.grids.walkableUnits.get(i)
}
