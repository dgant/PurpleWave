package Information.Grids.Movement

import Information.Grids.ArrayTypes.AbstractGridArray
import Mathematics.Physics.Force
import Mathematics.Points.Tile

class GridFlowField(origin: Tile) extends AbstractGridArray[Force] {

  final override val defaultValue: Force = Force(0, 0)
  final override val values: Array[Force] = Array.fill(length)(defaultValue)

  override def onInitialization(): Unit = {
    tiles.foreach(t => set(t, t.slowGroundDirectionTo(origin)))
  }
}
