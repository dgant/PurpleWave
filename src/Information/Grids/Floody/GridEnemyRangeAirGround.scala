package Information.Grids.Floody

import Mathematics.Maff
import ProxyBwapi.UnitInfo.UnitInfo

final class GridEnemyRangeAirGround extends AbstractGridFloody {

  override protected def include(unit: UnitInfo): Boolean = unit.isEnemy && unit.canAttack

  override protected def range(unit: UnitInfo): Int = Maff.div32(unit.pixelRangeMax.toInt)

  override val margin: Int = 3
}
