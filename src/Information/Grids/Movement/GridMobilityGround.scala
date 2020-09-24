package Information.Grids.Movement

import Information.Grids.AbstractGrid
import Lifecycle.With

class GridMobilityGround extends AbstractGrid[Int] {
  
  override def defaultValue: Int = 0
  
  override def get(i: Int): Int = {
    Seq(
      With.grids.mobilityTerrain.get(i),
      With.grids.mobilityAir.get(i)
    ).min
  }
}
