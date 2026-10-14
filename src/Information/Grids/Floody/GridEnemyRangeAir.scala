package Information.Grids.Floody

import Mathematics.Maff
import ProxyBwapi.UnitInfo.UnitInfo

final class GridEnemyRangeAir extends AbstractGridFloody {

  override protected def include(unit: UnitInfo): Boolean = unit.isEnemy && unit.canAttackAir

  override protected def range(unit: UnitInfo): Int = Maff.div32(unit.pixelRangeAir.toInt)

  // Allow for flyer size while pathfinding checks only the center line.
  override val margin: Int = 4
}

