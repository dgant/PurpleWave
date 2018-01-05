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
      val tiles = area.expand(a, a).tiles
      var i = 0
      while (i < tiles.length) {
        val j = tiles(i).i
        if (get(j) == defaultValue) {
          modifiedIndices += j
        }
        set(j, Math.min(get(j), a))
        i += 1
      }
      a -= 1
    }
    area.expand(4, 4).tiles
  }
}
