package Information.Grids.Vision

import Information.Grids.ArrayTypes.AbstractGridFramestamp
import Mathematics.Shapes.Circle
import ProxyBwapi.UnitInfo.UnitInfo

abstract class AbstractGridDetection extends AbstractGridFramestamp {
  
  override protected def updateCells() {
    detectors.foreach(detector => {
      val origin = detector.tile
      val points = Circle.points(1 + (if (detector.unitClass.isBuilding) 9 else 11))
      val nPoints = points.length
      var iPoint = 0
      while (iPoint < nPoints) {
        val tile = origin.add(points(iPoint))
        iPoint += 1
        if (tile.valid) {
          stamp(tile)
        }
      }
    })
  }
  
  protected def detectors: Iterable[UnitInfo]
}