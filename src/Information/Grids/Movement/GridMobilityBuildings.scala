package Information.Grids.Movement

import Information.Grids.ArrayTypes.AbstractGridArray
import Lifecycle.With
import Mathematics.Points.Tile
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable.ArrayBuffer

class GridMobilityBuildings extends AbstractGridArray[Int] {
  
  override val defaultValue: Int = 256
  override protected var values: Array[Int] = Array.fill(length) { defaultValue }
  override def onInitialization() {
    var i = 0
    while (i < length) {
      values(i) = 1 + new Tile(i).tileDistanceFromEdge
      i += 1
    }
  }
  
  val modifiedIndices = new ArrayBuffer[Int]
  
  override def update() {

    // DISABLED due to disuse and for performance
    return

    for (modifiedI <- modifiedIndices) {
      set(modifiedI, defaultValue)
    }
    modifiedIndices.clear()
    With.units.all.foreach(exclude)
  }
  
  @inline
  private def exclude(unit: UnitInfo) {
    if (unit.flying) return
    if ( ! unit.unitClass.isBuilding) return
    val area = unit.tileArea
    var a = 4
    while (a >= 0) {
      val unitClass = unit.unitClass
      val xUnit     = unit.tileTopLeft.x
      val yUnit     = unit.tileTopLeft.y
      val xUnitEnd  = xUnit + unitClass.tileWidth
      val yUnitEnd  = yUnit + unitClass.tileHeight
      val xAreaMin  = xUnit     - a
      val xAreaEnd  = xUnitEnd  + a
      val yAreaMin  = yUnit     - a
      val yAreaEnd  = yUnitEnd  + a
      
      var x = xAreaMin
      while (x < xAreaEnd) {
        var y = yAreaMin
        while (y < yAreaEnd) {
          if (x >= 0
            && y >= 0
            && x < With.mapTileWidth
            && y < With.mapTileHeight) {
            val j = x + y * With.mapTileWidth
            if (values(j) == defaultValue) {
              modifiedIndices += j
            }
            values(j) = Math.min(values(j), a)
          }
          y += 1
        }
        x += 1
      }
      a -= 1
    }
  }
}
