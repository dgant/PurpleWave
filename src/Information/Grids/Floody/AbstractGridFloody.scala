package Information.Grids.Floody

import Information.Grids.AbstractTypedGrid
import Lifecycle.With
import Mathematics.Points.Tile
import Mathematics.Shapes.Ring
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

/**
  * Base type for grids which measure a tile's distance from a unit.
  * Example: Grid of distance out of some enemy's range.
  * Source units have a tile and a range.
  * The grid updates only when either the tile or radius of the source unit changes, or the unit ceases to exist.
  */
abstract class AbstractGridFloody extends AbstractTypedGrid[Int] {
  final override val defaultValue: Int = 0
  @inline final def getUnchecked(i: Int): Int = tiles(i).maxDepth
  @inline final def inRange(tile: Tile): Boolean = inRange(tile.i)
  @inline final def inRange(i: Int): Boolean = if (valid(i)) inRangeUnchecked(i) else false
  @inline final def inRangeUnchecked(tile: Tile): Boolean = inRangeUnchecked(tile.i)
  @inline final def inRangeUnchecked(i: Int): Boolean = tiles(i).maxDepth > margin

  /**
   * How far out of a unit's radius to continue flooding
   */
  val margin: Int

  final val units = new mutable.HashMap[UnitInfo, FloodyUnit]()
  final val tiles: Array[FloodyTile] = (0 until length).map(new FloodyTile(_, margin)).toArray

  /**
    * Whether a unit should be considered in the grid at all
    */
  protected def include(unit: UnitInfo): Boolean

  /**
    * How far a unit's influence floods
    */
  protected def range(unit: UnitInfo): Int

  @inline final def floodiesNear(i: Int)      : Seq[FloodyUnitDepth]  = if (valid(i))   tiles(i)      .depths.view else Seq.empty
  @inline final def floodiesNear(tile: Tile)  : Seq[FloodyUnitDepth]  = if (tile.valid) tiles(tile.i) .depths.view else Seq.empty
  @inline final def unitsNear(i: Int)         : Seq[UnitInfo]         = floodiesNear(i)   .map(_.unit.unit)
  @inline final def unitsNear(tile: Tile)     : Seq[UnitInfo]         = floodiesNear(tile).map(_.unit.unit)
  @inline final def unitsOn(i: Int)           : Seq[UnitInfo]         = floodiesNear(i)   .filter(_.value >= margin).map(_.unit.unit)
  @inline final def unitsOn(tile: Tile)       : Seq[UnitInfo]         = floodiesNear(tile).filter(_.value >= margin).map(_.unit.unit)
  @inline final def dpfGround(tile: Tile)     : Double                = if (tile.valid) tiles(tile.i).dpfGround     else 0.0
  @inline final def dpfAir(tile: Tile)        : Double                = if (tile.valid) tiles(tile.i).dpfAir        else 0.0
  @inline final def damageGround(tile: Tile)  : Double                = if (tile.valid) tiles(tile.i).damageGround  else 0.0
  @inline final def damageAir(tile: Tile)     : Double                = if (tile.valid) tiles(tile.i).damageAir     else 0.0

  override def update(): Unit = {
    units.view.filterNot(f => shouldInclude(f._1)).toVector.foreach(f => removeUnit(f._2))
    With.units.all.foreach(updateUnit)
  }

  @inline private final def shouldInclude(unit: UnitInfo): Boolean = unit.likelyStillThere && include(unit)

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
        val floodyTile = tiles(tile.i)
        floodyTile.depths.add(FloodyUnitDepth(floody, value))
        floodyTile.maxDepth = Math.max(floodyTile.maxDepth, value) }
  }

  private final def removeUnit(floody: FloodyUnit): Unit = {
    units.remove(floody.unit)
    flood(floody).foreach {
      case (tile, value) =>
        val floodyTile = tiles(tile.i)
        floodyTile.depths.removeIf(_.unit.unit == floody.unit)
        if (value >= floodyTile.maxDepth) {
          var j = 0
          val size = floodyTile.depths.size
          floodyTile.maxDepth = defaultValue
          while (j < size) {
            floodyTile.maxDepth = Math.max(floodyTile.maxDepth, floodyTile.depths(j).value)
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
