package Information.Grids.Vision

import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo

class GridEnemyDetection extends AbstractGridDetection {
  
  override protected def detectors: Iterable[UnitInfo] =
    With.units.enemy.view
      .filter(unit =>
        unit.aliveAndComplete
        && unit.likelyStillThere
        && unit.unitClass.isDetector)
}