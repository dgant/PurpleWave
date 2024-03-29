package Micro.Coordination.Pathing

import Information.Grids.ArrayTypes.AbstractGridVersionedValue
import Lifecycle.With
import Mathematics.Points.Tile
import ProxyBwapi.UnitInfo.UnitInfo

class GridPathOccupancy extends AbstractGridVersionedValue[Int] {

  override final val defaultValue: Int = 0
  override val values: Array[Int] = Array.fill(length)(defaultValue)

  override protected def onUpdate(): Unit = {
    With.units.enemy.foreach(enemy => if (! enemy.flying && ! enemy.unitClass.isBuilding) addUnit(enemy, enemy.tile))
  }

  protected def add(tile: Tile, value: Int): Unit = set(tile, get(tile) + value)

  val neighborRatio = 0.25
  def addUnit(unit: UnitInfo, destination: Tile): Unit = {
    if (unit.airborne) return
    val neighborArea = (neighborRatio * unit.unitClass.sqrtArea).toInt
    add(destination, unit.unitClass.sqrtArea)
    add(destination.add(1, 0),  neighborArea)
    add(destination.add(0, 1),  neighborArea)
    add(destination.add(-1, 0), neighborArea)
    add(destination.add(0, -1), neighborArea)
  }
}