package Information.Grids.Floody

import ProxyBwapi.UnitInfo.UnitInfo

final class GridEnemyRangeAir extends AbstractGridFloody {

  override protected def include(unit: UnitInfo): Boolean = unit.isEnemy && unit.canAttackAir

  override protected def range(unit: UnitInfo): Int = unit.pixelRangeAir.toInt / 32

  override val margin: Int = 3
}

