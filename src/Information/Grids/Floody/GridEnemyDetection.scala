package Information.Grids.Floody

import ProxyBwapi.UnitInfo.UnitInfo

final class GridEnemyDetection extends AbstractGridFloody {

  override protected def include(unit: UnitInfo): Boolean = unit.complete && unit.unitClass.isDetector && ! unit.blind && unit.isEnemy

  override protected def range(unit: UnitInfo): Int = unit.sightPixels / 32

  override val margin: Int = 2
}