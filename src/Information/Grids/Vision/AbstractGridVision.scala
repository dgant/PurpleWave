package Information.Grids.Vision

import Mathematics.Shapes.Circle
import Information.Grids.ArrayTypes.AbstractGridBoolean
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.EnrichPosition._

abstract class AbstractGridVision extends AbstractGridBoolean {
  
  override def update() {
    reset()
    units
      .filter(_.possiblyStillThere)
      .foreach(u => {
      Circle.points(u.unitClass.sightRange / 32)
        .map(u.tileIncluding.add)
        .foreach(set(_, true))
    })
  }
  
  protected def units:Iterable[UnitInfo]
}