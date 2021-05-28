package Information.Grids.Floody

import ProxyBwapi.UnitInfo.UnitInfo

final class GridEnemyRangeAirGround extends AbstractGridFloody {

  override protected def include(unit: UnitInfo): Boolean = unit.isEnemy && unit.canAttack

  override protected def range(unit: UnitInfo): Int = unit.pixelRangeMax.toInt / 32

  override val margin: Int = 3
}
