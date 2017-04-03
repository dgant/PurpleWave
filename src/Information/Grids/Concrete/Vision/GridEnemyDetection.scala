package Information.Grids.Concrete.Vision

import Geometry.Shapes.Circle
import Information.Grids.Abstract.ArrayTypes.GridBoolean
import Startup.With
import Utilities.EnrichPosition._

class GridEnemyDetection extends GridBoolean {
  
  override def update() {
    reset()
    With.units.enemy
      .filter(_.possiblyStillThere)
      .filter(_.unitClass.isDetector)
      .foreach(u => {
      Circle.points(u.unitClass.sightRange / 32)
        .map(u.tileCenter.add)
        .foreach(set(_, true))
    })
  }
}