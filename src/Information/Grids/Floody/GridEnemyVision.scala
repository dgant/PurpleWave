package Information.Grids.Floody

import Mathematics.Maff
import ProxyBwapi.UnitInfo.UnitInfo

final class GridEnemyVision extends AbstractGridFloody {

  override protected def include(unit: UnitInfo): Boolean = unit.isEnemy

  override protected def range(unit: UnitInfo): Int = if (unit.blind || ! unit.complete) 1 else Maff.div32(unit.sightPixels)

  override val margin: Int = 2
}