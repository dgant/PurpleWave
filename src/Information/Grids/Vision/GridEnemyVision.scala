package Information.Grids.Vision

import Information.Grids.ArrayTypes.AbstractGridTimestamp
import Lifecycle.With
import Mathematics.Shapes.Circle

class GridEnemyVision extends AbstractGridTimestamp {
  
  override protected def updateTimestamps() {
    With.units.enemy
      .foreach(unit =>
        if (unit.aliveAndComplete && unit.likelyStillThere)
        Circle.points(unit.sightRangePixels/32)
          .map(unit.tileIncludingCenter.add)
          .foreach(set(_, frameUpdated)))
  }
}