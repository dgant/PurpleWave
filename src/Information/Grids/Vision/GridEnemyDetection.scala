package Information.Grids.Vision

import Information.Grids.ArrayTypes.AbstractGridVersionedValue
import Lifecycle.With
import Mathematics.Points.Tile
import Mathematics.Shapes.Ring

class GridEnemyDetection extends AbstractGridVersionedValue[Int] {

  override val defaultValue = 0
  override protected var values: Array[Int] = Array.fill(length)(defaultValue)

  // How far to extend the reach of range into negative territory
  val addedRange = 3

  // How many frames ahead to project positions
  val framesAhead = 12

  // More accurately `get(i) >= addedRange` but this should be a bit more conservative
  def isDetected(i: Int): Boolean = get(i) >= addedRange - 1
  def isDetected(tile: Tile): Boolean = isSet(tile.i)

  override def onUpdate(): Unit = {
    for (unit <- With.units.enemy) {
      if (unit.aliveAndComplete
        && unit.likelyStillThere
        && unit.unitClass.isDetector
        && ! unit.stasised
        && ! unit.blind) {
        val tileUnit = unit.projectFrames(framesAhead).tileIncluding
        val rangeMax = addedRange + (if (unit.unitClass.isBuilding) 7 else 11)
        for (d <- 1 to rangeMax) {
          for (point <- Ring.points(d)) {
            val tile = tileUnit.add(point)
            if (tile.valid) {
              val range = rangeMax - d
              set(tile, if (isSet(tile)) Math.max(range, get(tile)) else range)
            }
          }
        }
      }
    }
  }
}