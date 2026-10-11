package Information.Grids.Movement

import Information.Grids.ArrayTypes.AbstractGridArray
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Tile
import Mathematics.Shapes.Spiral

class GridGroundDistance(initialOrigins: Tile*) extends AbstractGridArray[Int] {

  final override val defaultValue: Int = 256 * 256
  final override val values: Array[Int] = Array.fill(length)(defaultValue)

  @inline final override def getUnchecked(i: Int): Int = values(i)
  override def reset(): Unit = java.util.Arrays.fill(values, defaultValue)

  def origins: Seq[Tile] = initialOrigins

  @inline final def walkable(tile: Tile): Boolean = walkable(tile.i)
  @inline final def walkable(iTile: Int): Boolean = With.paths.groundWalkable(iTile)

  override def onInitialization(): Unit = {
    // Mark on enqueue: each tile occupies the queue at most once.
    val open = new Array[Int](length)
    var head = 0
    var tail = 0
    val width = With.mapTileWidth
    def explore(iTile: Int, distance: Int): Unit = {
      if (valid(iTile) && getUnchecked(iTile) == defaultValue && walkable(iTile)) {
        values(iTile) = distance
        open(tail) = iTile
        tail += 1
      }
    }

    Maff.orElse(origins.filter(walkable), origins.flatMap(seed => Spiral(20).map(seed.add).filter(_.valid).find(walkable))).foreach(seed => explore(seed.i, 0))
    while (head < tail) {
      val here = open(head)
      head += 1
      val distance = getUnchecked(here) + 1
      if (here % width > 0)         explore(here - 1, distance)
      if (here % width < width - 1) explore(here + 1, distance)
      if (here >= width)           explore(here - width, distance)
      if (here + width < length)   explore(here + width, distance)
    }
  }
}
