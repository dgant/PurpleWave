package Information.Grids.Floody

import Mathematics.Maff
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.?

final class GridEnemyDetection extends AbstractGridFloody {

  override protected def include(unit: UnitInfo): Boolean = unit.complete && unit.unitClass.isDetector && ! unit.blind && unit.isEnemy

  override protected def range(unit: UnitInfo): Int = ?(unit.unitClass.isBuilding, 8, Maff.div32(unit.sightPixels)) // Nominally 7 for buildings but 8 might help us avoid dumb DT suicides

  override val margin: Int = 2
}