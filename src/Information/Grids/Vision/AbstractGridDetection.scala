package Information.Grids.Vision

import Information.Grids.ArrayTypes.AbstractGridTimestamp
import Mathematics.Shapes.Circle
import ProxyBwapi.UnitInfo.UnitInfo

abstract class AbstractGridDetection extends AbstractGridTimestamp {
  
  override protected def updateTimestamps() {
    detectors
      .foreach(detector =>
        Circle.points(11) // All detectors have an 11-tile detection range regardless of sight range
          .map(detector.tileIncludingCenter.add)
          .foreach(set(_, frameUpdated)))
  }
  
  protected def detectors: Traversable[UnitInfo]
}