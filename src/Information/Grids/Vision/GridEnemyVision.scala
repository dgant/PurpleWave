package Information.Grids.Vision

import Information.Grids.ArrayTypes.AbstractGridBoolean
import Lifecycle.With
import Mathematics.Shapes.Circle

class GridEnemyVision extends AbstractGridBoolean {
  
  override def update() {
    reset()
    With.units.enemy
      .filter(_.possiblyStillThere)
      .foreach(u => {
        Circle.points(u.unitClass.sightRange / 32)
          .map(u.tileIncludingCenter.add)
          .foreach(set(_, true))
      })
  }
}