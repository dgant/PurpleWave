package Information.Grids.Movement

import Information.Grids.AbstractGrid
import Lifecycle.With

class GridMobility extends AbstractGrid[Int] {
  
  override def defaultValue: Int = 0
  
  override def get(i: Int): Int = {
    Seq(
      With.grids.mobilityTerrain.get(i),
      With.grids.mobilityBuildings.get(i),
      With.grids.mobilityBorder.get(i)
    ).min
  }
}
