package Information.Grids.Floody

import Mathematics.Maff
import ProxyBwapi.UnitInfo.UnitInfo

final class GridFriendlyDetection extends AbstractGridFloody {

  override protected def include(unit: UnitInfo): Boolean = unit.complete && unit.unitClass.isDetector && ! unit.blind && unit.isFriendly

  override protected def range(unit: UnitInfo): Int = Maff.div32(unit.sightPixels)

  override val margin: Int = 2
}