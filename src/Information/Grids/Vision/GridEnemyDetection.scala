package Information.Grids.Vision

import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo

class GridEnemyDetection extends AbstractGridDetection {
  
  override protected def detectors: Seq[UnitInfo] =
    With.units.enemy.toSeq.filter(unit =>
      unit.aliveAndComplete
      && unit.likelyStillThere
      && unit.unitClass.isDetector)
}