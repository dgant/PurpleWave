package Micro.Coordination.Pathing

import Information.Grids.ArrayTypes.AbstractGridTimestampedValue
import Mathematics.Points.Tile
import ProxyBwapi.UnitInfo.UnitInfo

class GridPathOccupancy extends AbstractGridTimestampedValue[Int] {

  override protected var values: Array[Int] = Array.fill(indices.size)(0)

  override def defaultValue: Int = 0

  override protected def onUpdate(): Unit = {}

  protected def add(tile: Tile, value: Int): Unit = set(tile, get(tile) + value)

  val neighborRatio = 0.25
  def addUnit(unit: UnitInfo, destination: Tile): Unit = {
    val neighborArea = (neighborRatio * unit.unitClass.sqrtArea).toInt
    add(destination, unit.unitClass.sqrtArea.toInt)
    add(destination.add(1, 0),  neighborArea)
    add(destination.add(0, 1),  neighborArea)
    add(destination.add(-1, 0), neighborArea)
    add(destination.add(0, -1), neighborArea)
  }
}