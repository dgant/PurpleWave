package Information.Grids.Floody

import Information.Grids.AbstractGrid
import ProxyBwapi.UnitInfo.{CombatUnit, UnitInfo}
import ProxyBwapi.UnitTracking.UnorderedBuffer

import scala.collection.mutable

/**
  * Base type for grids which measure a tile's distance from a unit.
  * Example: Grid of distance out of some enemy's range.
  * Source units have a tile and a range.
  * The grid updates only when either the tile or radius of the source unit changes, or the unit ceases to exist.
  */
abstract class GridFloody extends AbstractGrid[Int] {
  private final val units = new mutable.HashMap[UnitInfo, FloodyUnit]()
  private final val tiles = Array.fill(length)(new UnorderedBuffer[FloodyUnit](12))
  private final val max = Array.fill(length)(0)
  @inline final def getUnchecked(i: Int): Int = max(i)
  @inline final override val defaultValue: Int = 0

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

  private final def addUnit(unit: FloodyUnit): Unit = {

  }

  private final def removeUnit(unit: FloodyUnit): Unit = {

  }
}
