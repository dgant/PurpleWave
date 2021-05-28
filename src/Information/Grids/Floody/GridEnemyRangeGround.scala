package Information.Grids.Floody

import ProxyBwapi.UnitInfo.UnitInfo

final class GridEnemyRangeGround extends AbstractGridFloody {

  override protected def include(unit: UnitInfo): Boolean = unit.isEnemy && unit.canAttackGround

  override protected def range(unit: UnitInfo): Int = unit.pixelRangeGround.toInt / 32

  override val margin: Int = 3
}
