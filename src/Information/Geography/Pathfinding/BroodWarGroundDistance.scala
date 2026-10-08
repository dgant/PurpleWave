package Information.Geography.Pathfinding

import Mathematics.Points.Pixel

import scala.collection.JavaConverters._
import scala.collection.mutable

object BroodWarGroundDistance {
  final case class Region(center: Pixel, group: Int, walkable: Boolean, neighbors: Vector[Int])
  val unreachable: Int = 0x7fff

  def apply(game: bwapi.Game): BroodWarGroundDistance = {
    val regions = game.getAllRegions.asScala.toVector
    val graph = new Array[Region](regions.map(_.getID).foldLeft(-1)(Math.max) + 1)
    regions.foreach(r => graph(r.getID) = Region(
      new Pixel(r.getCenter), r.getRegionGroupID, r.isAccessible, r.getNeighbors.asScala.map(_.getID).toVector))
    new BroodWarGroundDistance(graph, p => Option(game.getRegionAt(p.bwapi)).map(_.getID).getOrElse(-1))
  }

  // BW's integer distance, also applied to 8-bit fixed-point coordinates during search.
  private[Pathfinding] def length(dx: Int, dy: Int): Int = {
    val a = Math.max(Math.abs(dx), Math.abs(dy))
    val b = Math.min(Math.abs(dx), Math.abs(dy))
    if (a / 4 >= b) a else a - a / 16 + b * 3 / 8 - a / 64 + b * 3 / 256
  }
}

// Reproduce the region-path measurement used to admit Reaver shots, rather than tile
// walking distance. See OpenBW long_path_distance/pathfinder_find_long_path:
// https://github.com/OpenBW/openbw/blob/4b046d5f65302b10cb0a745f0fecd37ec85b20a8/bwgame.h#L2076
// BWAPI supplies the native graph and neighbor order, but rounds region centers to pixels.
final class BroodWarGroundDistance(graph: Array[BroodWarGroundDistance.Region], regionAt: Pixel => Int) {
  import BroodWarGroundDistance._

  private val cache = mutable.LongMap.empty[Int]
  private val seen = new Array[Int](graph.length)
  private val cost = new Array[Int](graph.length)
  private val remaining = new Array[Int](graph.length)
  private val parent = new Array[Int](graph.length)
  private val heapIndex = new Array[Int](graph.length)
  private val heap = new Array[Int](125)
  private val touched = new Array[Int](350)
  private var generation = 0

  // Cache exact endpoints: BW uses them to select a route, so a region-pair cache is unsound.
  def pixels(from: Pixel, to: Pixel): Int = synchronized {
    if (from.x < 0 || from.y < 0 || to.x < 0 || to.y < 0 || (from.x | from.y | to.x | to.y) > 65535) return unreachable
    val key = (from.x.toLong << 48) | (from.y.toLong << 32) | (to.x.toLong << 16) | to.y.toLong
    if (cache.size >= 4096) cache.clear()
    cache.getOrElseUpdate(key, search(from, to, regionAt(from), regionAt(to)))
  }

  private def search(from: Pixel, to: Pixel, source: Int, target: Int): Int = {
    def valid(id: Int): Boolean = id >= 0 && id < graph.length && graph(id) != null
    if (!valid(source) || !valid(target) || graph(source).group != graph(target).group) return unreachable
    if (source == target) return length(to.x - from.x, to.y - from.y)
    generation += 1
    if (generation == 0) { java.util.Arrays.fill(seen, 0); generation = 1 }
    var count = 0
    var size = 0
    var goal = -1
    def at(id: Int): Pixel = if (id == source) from else if (id == target) to else graph(id).center
    def estimate(id: Int): Int = cost(id) + remaining(id)
    def swap(a: Int, b: Int): Unit = {
      val id = heap(a); heap(a) = heap(b); heap(b) = id
      heapIndex(heap(a)) = a; heapIndex(heap(b)) = b
    }
    def up(start: Int): Unit = {
      var i = start
      while (i > 0 && estimate(heap((i - 1) / 2)) >= estimate(heap(i))) {
        val p = (i - 1) / 2; swap(i, p); i = p
      }
    }
    def push(id: Int): Unit = { heap(size) = id; heapIndex(id) = size; size += 1; up(size - 1) }
    def pop(): Int = {
      val result = heap(0)
      size -= 1
      if (size > 0) {
        heap(0) = heap(size); heapIndex(heap(0)) = 0
        var i = 0
        var done = false
        while (!done && 2 * i + 1 < size) {
          var child = 2 * i + 1
          if (child + 1 < size && estimate(heap(child + 1)) < estimate(heap(child))) child += 1
          if (estimate(heap(child)) >= estimate(heap(i))) done = true
          else { swap(i, child); i = child }
        }
      }
      heapIndex(result) = -1
      result
    }
    def discover(id: Int, previous: Int, total: Int): Unit = {
      seen(id) = generation; parent(id) = previous; cost(id) = total
      val p = at(id)
      remaining(id) = length((to.x - p.x) * 256, (to.y - p.y) * 256)
      touched(count) = id; count += 1
      push(id)
    }
    discover(source, -1, 0)
    while (size > 0 && goal < 0 && size < 125 && count < 350) {
      val current = pop()
      if (current == target) goal = current
      else {
        val p = at(current)
        def visit(id: Int): Unit = {
          if (size < 125 && count < 350) {
            val q = at(id)
            val step = length((q.x - p.x) * 256, (q.y - p.y) * 256) * (if (graph(id).group == graph(source).group) 1 else 2)
            val total = cost(current) + step
            if (seen(id) != generation) discover(id, current, total)
            else if (parent(current) != id && total < cost(id)) {
              parent(id) = current; cost(id) = total
              if (heapIndex(id) < 0) push(id) else up(heapIndex(id))
            }
          }
        }
        graph(current).neighbors.foreach(id => if (valid(id) && graph(id).walkable) visit(id))
        if (graph(current).group != graph(source).group) {
          graph(current).neighbors.foreach(id => if (valid(id) && !graph(id).walkable) visit(id))
        }
      }
    }
    if (goal < 0 && size > 0) goal = heap(0)
    if (goal < 0) {
      goal = source
      val p = graph(source).center
      remaining(source) = length((to.x - p.x) * 256, (to.y - p.y) * 256)
      var i = 1
      while (i < count) {
        if (remaining(touched(i)) < remaining(goal)) goal = touched(i)
        i += 1
      }
    }
    var nodes = 0
    var cursor = goal
    while (cursor >= 0) { nodes += 1; cursor = parent(cursor) }
    // BW retains at most the first 50 regions of a long path.
    while (nodes > 50) { goal = parent(goal); nodes -= 1 }
    if (nodes <= 2) return length(to.x - from.x, to.y - from.y)
    var distance = 0
    cursor = goal
    while (parent(cursor) >= 0) {
      val a = graph(cursor).center
      val b = graph(parent(cursor)).center
      distance += length(a.x - b.x, a.y - b.y)
      cursor = parent(cursor)
    }
    distance
  }
}
