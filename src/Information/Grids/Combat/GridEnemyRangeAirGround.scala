package Information.Grids.Combat

import Information.Grids.AbstractGrid
import Lifecycle.With

class GridEnemyRangeAirGround extends AbstractGrid[Int] {

  override def get(i: Int): Int = Math.max(
    With.grids.enemyRangeGround.get(i),
    With.grids.enemyRangeAir.get(i))

  override def defaultValue: Int = 0
}
