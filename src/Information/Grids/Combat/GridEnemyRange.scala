package Information.Grids.Combat

import Information.Grids.ArrayTypes.AbstractGridVersionedValue
import Lifecycle.With
import Mathematics.Shapes.Ring

class GridEnemyRange extends AbstractGridVersionedValue[Int] {

  override val defaultValue = 0
  override protected var values: Array[Int] = Array.fill(length)(defaultValue)

  // How far to extend the reach of range into negative territory
  val addedRange = 5

  override def onUpdate(): Unit = {
    for (unit <- With.units.enemy) {
      if (unit.likelyStillThere && (unit.canAttack || unit.unitClass.spells.nonEmpty)) {
        val tileUnit = unit.tileIncludingCenter
        val rangeMax = addedRange + unit.effectiveRangePixels.toInt/32
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
