package Information.Grids.Floody

import Information.Grids.AbstractGrid
import Mathematics.Points.Tile
import Mathematics.Shapes.Ring
import ProxyBwapi.UnitInfo.{CombatUnit, UnitInfo}
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
  @inline final def getUnchecked(i: Int): Int = values(i)
  @inline final override val defaultValue: Int = 0
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
  protected def include(unit: CombatUnit): Boolean

  /**
    * How far a unit's influence floods
    */
  protected def radius(unit: CombatUnit): Int

  final override def update(): Unit = {
    units.view.filterNot(f => f._1.alive && f._1.likelyStillThere && include(f._1)).toVector.foreach(f => removeUnit(f._2))
  }

  final def updateUnit(unit: UnitInfo): Unit = {
    val floodyOld = units.get(unit)
    lazy val unitRadius = radius(unit)
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
        tiles(tile.i).removeIf(_.unit == floody.unit)
        values(tile.i) = ByOption.max(tiles(tile.i).view.map(_.value)).getOrElse(defaultValue) }
  }

  @inline private final def flood(floody: FloodyUnit): Seq[(Tile, Int)] = {
    val max = floody.radius + margin
    (1 to max).flatMap(d => Ring.points(d).map(p => (floody.tile.add(p), max - d))).filter(_._1.valid)
  }
}
