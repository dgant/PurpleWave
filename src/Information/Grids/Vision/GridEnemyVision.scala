package Information.Grids.Vision

import Information.Grids.ArrayTypes.AbstractGridFramestamp
import Lifecycle.With
import Mathematics.Points.Tile
import Mathematics.Shapes.Circle

class GridEnemyVision extends AbstractGridFramestamp {

  def hasSeen(tile: Tile): Boolean = get(tile) > 0
  def hasSeen(i: Int): Boolean = get(i) > 0
  
  override protected def updateCells() {
    With.units.enemy.foreach(unit =>
      if (unit.aliveAndComplete && unit.likelyStillThere) {
        val start = unit.tile
        val altitude = start.altitude
        val points = Circle.points(unit.sightPixels/32)
        val nPoints = points.length
        var iPoint = 0
        while (iPoint < nPoints) {
          val tile = start.add(points(iPoint))
          iPoint += 1
          if (tile.valid && (unit.flying || altitude >= tile.altitude)) {
            stamp(tile)
          }
        }
      })
  }
}