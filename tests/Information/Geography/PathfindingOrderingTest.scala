package Information.Geography.Pathfinding

import Lifecycle.With
import Mathematics.Points.Tile

import scala.collection.mutable
import scala.util.Random

object PathfindingOrderingTest extends App {
  With.mapTileWidth = 128
  With.mapTileHeight = 128
  With.paths = new Paths
  val original = Ordering.by[TileState, Double](t => -t.costToEndFloor)
  val optimized = TileStateOrdering
  def state(i: Int, cost: Double): TileState = {
    val result = new TileState(new Tile(i))
    result.setCostToEndFloor(cost)
    result
  }
  def compare(a: Double, b: Double): Unit = {
    val left = state(0, a)
    val right = state(1, b)
    assert(original.compare(left, right) == optimized.compare(left, right), (a, b))
    assert(original.lt(left, right) == optimized.lt(left, right), (a, b))
    assert(original.lteq(left, right) == optimized.lteq(left, right), (a, b))
    assert(original.gt(left, right) == optimized.gt(left, right), (a, b))
    assert(original.gteq(left, right) == optimized.gteq(left, right), (a, b))
    assert(original.equiv(left, right) == optimized.equiv(left, right), (a, b))
  }
  val special = Vector(Double.NaN, Double.PositiveInfinity, Double.NegativeInfinity,
    0.0, -0.0, 1.0, -1.0, Double.MaxValue, -Double.MaxValue,
    java.lang.Double.MIN_VALUE, -java.lang.Double.MIN_VALUE)
  special.foreach(a => special.foreach(b => compare(a, b)))
  val random = new Random(42)
  (0 until 20000).foreach(_ => compare(
    java.lang.Double.longBitsToDouble(random.nextLong()),
    java.lang.Double.longBitsToDouble(random.nextLong())))
  println("PASS all comparison operations match the original, including NaN and signed zero")

  (0 until 100).foreach { trial =>
    val oldQueue = new mutable.PriorityQueue[TileState]()(original)
    val newQueue = new mutable.PriorityQueue[TileState]()(optimized)
    val nodes = (0 until 1024).map(i => state(i, random.nextInt(32).toDouble)).toArray
    nodes.foreach { node => oldQueue.enqueue(node); newQueue.enqueue(node) }
    var count = 0
    while (oldQueue.nonEmpty) {
      // A* updates enqueued scores in place; preserve that behavior and tie order.
      if (count % 7 == 0) nodes(random.nextInt(nodes.length)).setCostToEndFloor(random.nextInt(32).toDouble)
      assert(oldQueue.dequeue() eq newQueue.dequeue(), (trial, count))
      count += 1
    }
    assert(newQueue.isEmpty)
  }
  println("PASS 102400 dequeues retain exact node identity with ties and in-place cost changes")
}
