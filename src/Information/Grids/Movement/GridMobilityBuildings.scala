package Information.Grids.Movement

import Information.Grids.AbstractGrid
import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

class GridMobilityBuildings extends AbstractGrid[Int] {
  
  val values = new mutable.HashMap[Int, Int] {
    override def default(key: Int): Int = defaultValue
  }
  
  override def update() {
    values.clear()
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
        values(j) = Math.min(values(j), a)
        i += 1
      }
      a -= 1
    }
    area.expand(4, 4).tiles
  }
  
  override def get(i: Int): Int = values(i)
  
  override def defaultValue: Int = 256
}
