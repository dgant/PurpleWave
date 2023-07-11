package Information.Grids.Floody

import Mathematics.Maff
import ProxyBwapi.UnitInfo.UnitInfo

final class GridEnemyRangeGround extends AbstractGridFloody {

  override protected def include(unit: UnitInfo): Boolean = unit.isEnemy && unit.canAttackGround

  override protected def range(unit: UnitInfo): Int = Maff.div32(unit.pixelRangeGround.toInt)

  override val margin: Int = 3
}
