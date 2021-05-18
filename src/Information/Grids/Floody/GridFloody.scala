package Information.Grids.Floody

import Information.Grids.AbstractGrid
import Mathematics.Points.Tile
import Mathematics.Shapes.Ring
import ProxyBwapi.UnitInfo.UnitInfo
import ProxyBwapi.UnitTracking.UnorderedBuffer
import Utilities.ByOption

import scala.collection.mutable

/**
  * Base type for grids which measure a tile's distance from a unit.
  * Example: Grid of distance out of some enemy's range.
  * Source units have a tile and a range.
  * The grid updates only when either the tile or radius of the source unit changes, or the unit ceases to exist.
  */
abstract class GridFloody extends AbstractGrid[Int] {
  @inline final override val defaultValue: Int = 0
  @inline final def getUnchecked(i: Int): Int = values(i)
  @inline final def inRange(tile: Tile): Boolean = inRange(tile.i)
  @inline final def inRange(i: Int): Boolean = if (valid(i)) inRangeUnchecked(i) else false
  @inline final def inRangeUnchecked(tile: Tile): Boolean = inRangeUnchecked(tile.i)
  @inline final def inRangeUnchecked(i: Int): Boolean = values(i) > margin

  private final val units = new mutable.HashMap[UnitInfo, FloodyUnit]()
  private final val tiles = Array.fill(length)(new UnorderedBuffer[FloodyTile](12))
  private final val values = Array.fill(length)(defaultValue)

  /**
   * How far out of a unit's radius to continue flooding
   */
  val margin: Int = 0

  /**
    * Whether a unit should be considered in the grid at all
    */
  protected def include(unit: UnitInfo): Boolean

  /**
    * How far a unit's influence floods
    */
  protected def range(unit: UnitInfo): Int

  final override def update(): Unit = {
    units.view.filterNot(f => f._1.alive && f._1.likelyStillThere && include(f._1)).toVector.foreach(f => removeUnit(f._2))
  }

  private final def shouldInclude(unit: UnitInfo): Boolean = unit.alive && unit.likelyStillThere && include(unit)

  final def updateUnit(unit: UnitInfo): Unit = {
    val floodyOld = units.get(unit)
    lazy val unitRadius = range(unit)
    lazy val floodyNew = FloodyUnit(unit, unitRadius, unit.tile)
    if (floodyOld.isDefined) {
      if (unit.tile != floodyOld.get.tile || unitRadius != floodyOld.get.radius) {
        removeUnit(floodyOld.get)
        addUnit(floodyNew)
      }
    } else {
      addUnit(floodyNew)
    }
  }

  private final def addUnit(floody: FloodyUnit): Unit = {
    flood(floody).foreach {
      case (tile, value) =>
        tiles(tile.i).add(FloodyTile(floody, value))
        values(tile.i) = Math.max(values(tile.i), value) }
  }

  private final def removeUnit(floody: FloodyUnit): Unit = {
    flood(floody).foreach {
      case (tile, value) =>
        tiles(tile.i).removeIf(_.unit.unit == floody.unit)
        values(tile.i) = ByOption.max(tiles(tile.i).view.map(_.value)).getOrElse(defaultValue) }
  }

  @inline private final def flood(floody: FloodyUnit): Seq[(Tile, Int)] = {
    val max = floody.radius + margin
    (1 to max).flatMap(d => Ring.points(d).map(p => (floody.tile.add(p), max - d))).filter(_._1.valid)
  }
}
