package Information.Grids.Movement

import Information.Grids.ArrayTypes.AbstractGridArray
import Lifecycle.With
import Mathematics.Points.Tile

class GridMobilityTerrain extends AbstractGridArray[Int] {
  
  override def onInitialization() {
    var mobility    = 0
    var horizon     = (0 until length).filterNot(With.grids.walkableTerrain.get).toArray
    val explored    = new Array[Boolean](length)
    
    while (horizon.nonEmpty) {
      if (mobility > 0) {
        horizon.foreach(set(_, mobility))
      }
      horizon.foreach(explored(_) = true)
      horizon = horizon.flatMap(new Tile(_).adjacent4).filterNot(explored.contains).map(_.i)
      mobility += 1
    }
  }
  
  override protected var values: Array[Int] = Array.fill(length) { defaultValue }
  override def defaultValue: Int = 0
}
