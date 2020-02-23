package Information.Grids.Vision

import Information.Grids.ArrayTypes.AbstractGridFramestamp
import Lifecycle.With
import Mathematics.Shapes.Circle

class GridEnemyVision extends AbstractGridFramestamp {
  
  override protected def updateTimestamps() {
    With.units.enemy.foreach(unit =>
      if (unit.aliveAndComplete && unit.likelyStillThere) {
        val start = unit.tileIncludingCenter
        val altitude = With.grids.altitudeBonus.rawValues(start.i)
        val points = Circle.points(unit.sightRangePixels/32)
        val nPoints = points.length
        var iPoint = 0
        while (iPoint < nPoints) {
          val tile = start.add(points(iPoint))
          iPoint += 1
          if (tile.valid && (unit.flying || altitude >= With.grids.altitudeBonus.rawValues(tile.i))) {
            stamp(tile)
          }
        }
      })
  }
}