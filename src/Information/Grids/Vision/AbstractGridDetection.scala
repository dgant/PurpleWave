package Information.Grids.Vision

import Information.Grids.ArrayTypes.AbstractGridBoolean
import Lifecycle.With
import Mathematics.Shapes.Circle
import ProxyBwapi.UnitInfo.UnitInfo

abstract class AbstractGridDetection extends AbstractGridBoolean {
  
  var lastUpdateFrame = 0
  
  override def update() {
    lastUpdateFrame = With.frame
    reset()
    units
      .filter(_.likelyStillThere)
      .filter(unit =>
        unit.likelyStillThere &&
        unit.aliveAndComplete &&
        unit.unitClass.isDetector)
      .foreach(u => {
      Circle.points(11) // All detectors have a detection range of 11 even if their sight range is different. In practice this is closer to 10
        .map(u.tileIncludingCenter.add)
        .foreach(set(_, true))
    })
  }
  
  protected def units: Seq[UnitInfo]
}