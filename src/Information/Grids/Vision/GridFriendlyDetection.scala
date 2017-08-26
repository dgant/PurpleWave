package Information.Grids.Vision

import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo

class GridFriendlyDetection extends AbstractGridDetection {
  
  override protected def detectors: Seq[UnitInfo] =
    With.units.ours.toSeq.filter(unit =>
      unit.aliveAndComplete
      && unit.unitClass.isDetector)
}