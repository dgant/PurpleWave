package Information.Grids.Dpf

import Information.Grids.AbstractGrid
import Lifecycle.With

class GridDpfEnemyApproximate extends AbstractGrid[Double] {
  
  override def get(i: Int): Double = {
    With.grids.dpfEnemyAirNormal        .get(i) +
    With.grids.dpfEnemyAirExplosive     .get(i) +
    With.grids.dpfEnemyAirConcussive    .get(i) +
    With.grids.dpfEnemyGroundNormal     .get(i) +
    With.grids.dpfEnemyGroundExplosive  .get(i) +
    With.grids.dpfEnemyGroundConcussive .get(i)
  }
  
  override def defaultValue: Double = 0.0
  
  override def repr(value: Double): String = value.toInt.toString
}
