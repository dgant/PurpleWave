package Micro.Coordination.Pathing

import Information.Grids.ArrayTypes.AbstractGridTimestampedValue
import Mathematics.Points.Tile
import ProxyBwapi.UnitInfo.UnitInfo

class GridPathOccupancy extends AbstractGridTimestampedValue[Int] {

  override protected var values: Array[Int] = Array.fill(indices.size)(0)

  override def defaultValue: Int = 0

  override protected def onUpdate(): Unit = {}

  def addUnit(unit: UnitInfo, destination: Tile): Unit = {
    set(destination, get(destination) + unit.unitClass.sqrtArea)
  }
}