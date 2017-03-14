package Geometry.Grids.Real

import Geometry.Grids.Abstract.Grid
import Startup.With

class GridBuildable extends Grid[Boolean] {
  override def get(i: Int): Boolean = With.grids.buildableTerrain.get(i) && With.grids.walkableUnits.get(i)
}
