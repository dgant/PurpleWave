package Information.Grids.Vision

import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo

class GridEnemyDetection extends AbstractGridDetection {
  
  override protected def units: Seq[UnitInfo] = With.units.enemy.toSeq
    .filter(unit =>
      unit.likelyStillThere &&
      unit.aliveAndComplete &&
      unit.unitClass.isDetector)
}