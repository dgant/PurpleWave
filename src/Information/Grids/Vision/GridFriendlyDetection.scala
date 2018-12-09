package Information.Grids.Vision

import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo

class GridFriendlyDetection extends AbstractGridDetection {
  
  override protected def detectors: Iterable[UnitInfo] =
    With.units.ours
      .view
      .filter(unit =>
        unit.aliveAndComplete
        && unit.unitClass.isDetector)
}