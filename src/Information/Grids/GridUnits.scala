package Information.Grids

import Lifecycle.With
import Mathematics.Points.Tile
import ProxyBwapi.UnitInfo.UnitInfo
import ProxyBwapi.UnitTracking.UnorderedBuffer

import scala.collection.mutable

final class GridUnits extends AbstractTypedGrid[Traversable[UnitInfo]] {
  @inline override val defaultValue: Traversable[UnitInfo] = Traversable.empty
  @inline def getUnchecked(i: Int): Traversable[UnitInfo] = values(i)
  private final val values = Array.fill(length)(new UnorderedBuffer[UnitInfo](12))
  private final val units = new mutable.HashMap[UnitInfo, Tile]()
  override def repr(value: Traversable[UnitInfo]): String = value.size.toString

  override def update(): Unit = {
    units.view.filterNot(_._1.likelyStillThere).toVector.foreach(u => removeUnit(u._1, u._2))
    With.units.all.filter(shouldInclude).foreach(updateUnit)
  }

  private def shouldInclude(unit: UnitInfo): Boolean = unit.alive && unit.likelyStillThere

  def updateUnit(unit: UnitInfo): Unit = {
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

  private def addUnit(unit: UnitInfo): Unit = {
    val tile = unit.tile
    // There's an offchance of invalid tiles if invoked on a ghost unit
    if (tile.valid) {
      values(tile.i).add(unit)
    }
    units(unit) = tile
  }

  private def removeUnit(unit: UnitInfo, tile: Tile): Unit = {
    if (tile.valid) {
      values(tile.i).remove(unit)
    }
    units.remove(unit)
  }
}
