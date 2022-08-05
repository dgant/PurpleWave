package Information.Grids.Floody

import Information.Grids.AbstractTypedGrid
import Lifecycle.With
import Mathematics.Points.Tile
import Mathematics.Shapes.Ring
import ProxyBwapi.UnitInfo.UnitInfo
import ProxyBwapi.UnitTracking.UnorderedBuffer

import scala.collection.mutable

/**
  * Base type for grids which measure a tile's distance from a unit.
  * Example: Grid of distance out of some enemy's range.
  * Source units have a tile and a range.
  * The grid updates only when either the tile or radius of the source unit changes, or the unit ceases to exist.
  */
abstract class AbstractGridFloody extends AbstractTypedGrid[Int] {
  final override val defaultValue: Int = 0
  @inline final def getUnchecked(i: Int): Int = maxValues(i)
  @inline final def inRange(tile: Tile): Boolean = inRange(tile.i)
  @inline final def inRange(i: Int): Boolean = if (valid(i)) inRangeUnchecked(i) else false
  @inline final def inRangeUnchecked(tile: Tile): Boolean = inRangeUnchecked(tile.i)
  @inline final def inRangeUnchecked(i: Int): Boolean = maxValues(i) > margin

  final val units = new mutable.HashMap[UnitInfo, FloodyUnit]()
  final val tiles = Array.fill(length)(new UnorderedBuffer[FloodyTile](12))
  final val maxValues = Array.fill(length)(defaultValue)

  /**
   * How far out of a unit's radius to continue flooding
   */
  val margin: Int

  /**
    * Whether a unit should be considered in the grid at all
    */
  protected def include(unit: UnitInfo): Boolean

  /**
    * How far a unit's influence floods
    */
  protected def range(unit: UnitInfo): Int

  def floodiesNear(i: Int)      : Seq[FloodyTile] = if (valid(i))   tiles(i)      .view else Seq.empty
  def floodiesNear(tile: Tile)  : Seq[FloodyTile] = if (tile.valid) tiles(tile.i) .view else Seq.empty
  def unitsNear(i: Int)         : Seq[UnitInfo]   = floodiesNear(i)   .map(_.unit.unit)
  def unitsNear(tile: Tile)     : Seq[UnitInfo]   = floodiesNear(tile).map(_.unit.unit)
  def unitsOn(i: Int)           : Seq[UnitInfo]   = floodiesNear(i)   .filter(_.value >= margin).map(_.unit.unit)
  def unitsOn(tile: Tile)       : Seq[UnitInfo]   = floodiesNear(tile).filter(_.value >= margin).map(_.unit.unit)

  override def update(): Unit = {
    units.view.filterNot(f => shouldInclude(f._1)).toVector.foreach(f => removeUnit(f._2))
    With.units.all.foreach(updateUnit)
  }

  private final def shouldInclude(unit: UnitInfo): Boolean = unit.likelyStillThere && include(unit)

  @inline private final def updateUnit(unit: UnitInfo): Unit = {
    if (shouldInclude(unit)) {
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
  }

  private final def addUnit(floody: FloodyUnit): Unit = {
    units(floody.unit) = floody
    flood(floody).foreach {
      case (tile, value) =>
        tiles(tile.i).add(FloodyTile(floody, value))
        maxValues(tile.i) = Math.max(maxValues(tile.i), value) }
  }

  private final def removeUnit(floody: FloodyUnit): Unit = {
    units.remove(floody.unit)
    flood(floody).foreach {
      case (tile, value) =>
        val i = tile.i
        tiles(i).removeIf(_.unit.unit == floody.unit)
        if (value >= maxValues(i)) {
          var j = 0
          val size = tiles(i).size
          maxValues(i) = defaultValue
          while (j < size) {
            maxValues(i) = Math.max(maxValues(i), tiles(i)(j).value)
            j += 1
          }
        }
    }
  }

  @inline private final def flood(floody: FloodyUnit): Seq[(Tile, Int)] = {
    val max = floody.radius + margin
    (0 to max).flatMap(d => Ring(d).map(p => (floody.tile.add(p), max - d))).filter(_._1.valid)
  }
}
