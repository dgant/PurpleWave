package Information.Grids.Movement

import Information.Grids.AbstractGrid
import Lifecycle.With

class GridMobilityForceGround extends AbstractGridMobilityForce {
  
  override protected def underlyingGrid: AbstractGrid[Int] = With.grids.mobilityGround
}
