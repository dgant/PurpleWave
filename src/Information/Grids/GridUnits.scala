package Information.Grids

import Mathematics.Points.Tile
import ProxyBwapi.UnitInfo.UnitInfo
import ProxyBwapi.UnitTracking.UnorderedBuffer
import Utilities.?

import scala.collection.mutable

final class GridUnits extends AbstractTypedGrid[Traversable[UnitInfo]] with GridUpdateUnit{
  @inline override val defaultValue: Traversable[UnitInfo] = Traversable.empty
  @inline def getUnchecked(i: Int): Traversable[UnitInfo] = values(i)
  private final val values = Array.fill(length)(new UnorderedBuffer[UnitInfo](12))
  private final val units = new mutable.HashMap[UnitInfo, Tile]()
  override def repr(value: Traversable[UnitInfo]): String = value.size.toString

  override def update(): Unit = {
    units.view.filterNot(p => shouldInclude(p._1)).toVector.foreach(u => removeUnit(u._1, u._2))
    // Now that units are invoking this on changePixel we can probably ditch these updates
    // With.units.all.filter(shouldInclude).foreach(updateUnit)
  }

  private def shouldInclude(unit: UnitInfo): Boolean = unit.alive && unit.likelyStillThere

  def updateUnit(unit: UnitInfo): Unit = {
    val tileOld = units.get(unit)
    if (tileOld.isDefined) {
      if (unitTile(unit) != tileOld.get) {
        removeUnit(unit, tileOld.get)
        if (shouldInclude(unit)) {
          addUnit(unit)
        }
      }
    } else {
      addUnit(unit)
    }
  }

  private def addUnit(unit: UnitInfo): Unit = {
    // Stinky approach: Put buildings on their top-left tile so we know exactly when a placement has been occupied.
    val tile = unitTile(unit)
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

  private def unitTile(unit: UnitInfo): Tile = ? (unit.unitClass.isBuilding, unit.tileTopLeft, unit.tile)
}
