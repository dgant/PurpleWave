package Information.Grids.Movement

import Information.Grids.AbstractTypedGrid
import Mathematics.Physics.Force
import Mathematics.Points.Tile

class GridFlowField(origin: Tile) extends AbstractTypedGrid[Force] {

  final override val defaultValue: Force = Force(0, 0)
  @volatile private var values: Array[Force] = new Array[Force](length)

  def invalidate(): Unit = { values = new Array[Force](length) }

  @inline final override def getUnchecked(i: Int): Force = {
    // Only calculate cells actually visited by the simulator. Swapping the array
    // keeps an in-flight asynchronous calculation from repopulating a newer cache.
    val cache = values
    var force = cache(i)
    if (force == null) {
      force = new Tile(i).slowGroundDirectionTo(origin)
      cache(i) = force
    }
    force
  }
}
