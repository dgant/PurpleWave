package Information.Grids.Vision

import Information.Grids.ArrayTypes.AbstractGridTimestamp
import Mathematics.Shapes.Circle
import ProxyBwapi.UnitInfo.UnitInfo

abstract class AbstractGridDetection extends AbstractGridTimestamp {
  
  override protected def updateTimestamps() {
    detectors
      .foreach(detector =>
        Circle.points(1 + (if (detector.unitClass.isBuilding) 9 else 11))
          .map(detector.tileIncludingCenter.add)
          .foreach(stamp))
  }
  
  protected def detectors: Traversable[UnitInfo]
}