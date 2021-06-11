package Information.Grids.Floody

import ProxyBwapi.UnitInfo.UnitInfo

final class GridEnemyVision extends AbstractGridFloody {

  override protected def include(unit: UnitInfo): Boolean = unit.isEnemy

  override protected def range(unit: UnitInfo): Int = if (unit.blind || ! unit.complete) 1 else unit.sightPixels / 32

  override val margin: Int = 2
}