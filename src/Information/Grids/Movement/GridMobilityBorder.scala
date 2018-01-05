package Information.Grids.Movement

import Information.Grids.ArrayTypes.AbstractGridArray
import Mathematics.Points.Tile

class GridMobilityBorder extends AbstractGridArray[Int] {
  
  override def onInitialization() {
    var i = 0
    while (i < length) {
      values(i) = 1 + new Tile(i).tileDistanceFromEdge
      i += 1
    }
  }
  
  override protected var values: Array[Int] = Array.fill(length) { defaultValue }
  override def defaultValue: Int = 0
}
