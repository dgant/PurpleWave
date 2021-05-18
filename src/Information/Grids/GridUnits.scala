package Information.Grids

import Mathematics.Points.Tile
import ProxyBwapi.UnitInfo.UnitInfo
import ProxyBwapi.UnitTracking.UnorderedBuffer

import scala.collection.mutable

class GridUnits extends AbstractGrid[Traversable[UnitInfo]] {
  @inline final override val defaultValue: Traversable[UnitInfo] = Traversable.empty
  @inline final def getUnchecked(i: Int): Traversable[UnitInfo] = values(i)
  private final val values = Array.fill(length)(new UnorderedBuffer[UnitInfo](12))
  private final val units = new mutable.HashMap[UnitInfo, Tile]()

  final override def update(): Unit = {
    units.view.filterNot(_._1.likelyStillThere).toVector.foreach(u => removeUnit(u._1, u._2))
  }

  private final def shouldInclude(unit: UnitInfo): Boolean = unit.alive && unit.likelyStillThere

  final def updateUnit(unit: UnitInfo): Unit = {
    val tileOld = units.get(unit)
    if (tileOld.isDefined) {
      if (unit.tile != tileOld.get) {
        removeUnit(unit, tileOld.get)
        addUnit(unit)
      }
    } else {
      addUnit(unit)
    }
  }

  private final def addUnit(unit: UnitInfo): Unit = {
    val tile = unit.tile
    values(tile.i).add(unit)
    units(unit) = tile
  }

  private final def removeUnit(unit: UnitInfo, tile: Tile): Unit = {
    values(tile.i).remove(unit)
    units.remove(unit)
  }
}
