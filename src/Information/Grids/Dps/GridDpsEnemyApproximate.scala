package Information.Grids.Dps

import Information.Grids.AbstractGrid
import Lifecycle.With

class GridDpsEnemyApproximate extends AbstractGrid[Double] {
  
  override def get(i: Int): Double = {
    With.grids.dpsEnemyAirNormal        .get(i) +
    With.grids.dpsEnemyAirExplosive     .get(i) +
    With.grids.dpsEnemyAirConcussive    .get(i) +
    With.grids.dpsEnemyGroundNormal     .get(i) +
    With.grids.dpsEnemyGroundExplosive  .get(i) +
    With.grids.dpsEnemyGroundConcussive .get(i)
  }
  
  override def defaultValue: Double = 0.0
  
  override def repr(value: Double): String = value.toInt.toString
}
