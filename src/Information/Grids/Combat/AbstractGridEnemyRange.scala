package Information.Grids.Combat

import Information.Grids.ArrayTypes.AbstractGridVersionedValue
import Lifecycle.With
import Mathematics.Shapes.Ring
import ProxyBwapi.UnitInfo.UnitInfo

abstract class AbstractGridEnemyRange extends AbstractGridVersionedValue[Int] {

  override val defaultValue = 0
  override protected var values: Array[Int] = Array.fill(length)(defaultValue)

  // How far to extend the reach of range into negative territory
  val addedRange = 5

  // How many frames ahead to project positions
  val framesAhead = 12

  protected def pixelRangeMax(unit: UnitInfo): Double

  override def onUpdate(): Unit = {
    for (unit <- With.units.enemy) {
      if (unit.likelyStillThere && (unit.canAttack || unit.unitClass.spells.nonEmpty) && unit.battle.nonEmpty) {
        val tileUnit = unit.projectFrames(framesAhead).tileIncluding
        val rangeMax = addedRange + pixelRangeMax(unit).toInt/32
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
