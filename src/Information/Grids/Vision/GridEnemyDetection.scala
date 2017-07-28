package Information.Grids.Vision

import Mathematics.Shapes.Circle
import Information.Grids.ArrayTypes.AbstractGridBoolean
import Lifecycle.With

class GridEnemyDetection extends AbstractGridBoolean {
  
  override def update() {
    reset()
    With.units.enemy
      .filter(_.likelyStillThere)
      .filter(unit =>
        unit.likelyStillThere &&
        unit.aliveAndComplete &&
        unit.unitClass.isDetector)
      .foreach(u => {
      Circle.points(11) // All detectors have a detection range of 11 even if their sight range is different
        .map(u.tileIncludingCenter.add)
        .foreach(set(_, true))
    })
  }
}