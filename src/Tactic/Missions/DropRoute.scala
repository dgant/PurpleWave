package Tactic.Missions

import Information.Geography.Pathfinding.Types.TilePath
import Lifecycle.With
import Mathematics.Points.{Pixel, Tile}
import Utilities.Time.Seconds

import scala.collection.mutable

private[Missions] class DropRoute {
  private var destination: Option[Tile] = None
  private var path: Option[TilePath] = None
  private var values = Vector.empty[(Int, Boolean)]
  private var progress: Option[Tile] = None
  private var progressFrame = With.frame
  private var failureFrame: Option[Int] = None

  def stalled: Boolean = failureFrame.exists(With.framesSince(_) >= Seconds(8)())
  private def value(tile: Tile): (Int, Boolean) = (With.grids.enemyRangeAir.get(tile), tile.visibleToEnemy)
  private def exposure(tile: Tile): Double = 64.0 * (With.grids.enemyRangeAir.get(tile) + (if (tile.visibleToEnemy) 1 else 0))
  private def centerCost(tile: Tile): Double = {
    val depth = Seq(tile.x, tile.y, With.mapTileWidth - 1 - tile.x, With.mapTileHeight - 1 - tile.y).min
    val relative = 2.0 * depth / Math.min(With.mapTileWidth, With.mapTileHeight)
    12.0 * relative * relative
  }
  private def line(from: Tile, to: Tile): Vector[Tile] = {
    val steps = Math.max(Math.abs(to.x - from.x), Math.abs(to.y - from.y))
    if (steps == 0) Vector(from) else (0 to steps).map(i => Tile(
      Math.round(from.x + (to.x - from.x) * i.toDouble / steps).toInt,
      Math.round(from.y + (to.y - from.y) * i.toDouble / steps).toInt)).toVector
  }

  // Search the whole map on a four-tile air grid. A short greedy search cannot
  // compare the two perimeter approaches or see the far end of an enemy screen.
  private def find(from: Tile, to: Tile): TilePath = {
    val xs = (2 until With.mapTileWidth - 1 by 4).toVector
    val ys = (2 until With.mapTileHeight - 1 by 4).toVector
    val grid = (for (y <- ys; x <- xs) yield Tile(x, y)).toVector
    val start = grid.size
    val goal = start + 1
    val nodes = grid ++ Vector(from, to)
    def nearby(t: Tile): Vector[Int] = grid.indices.sortBy(i => grid(i).tileDistanceSquared(t)).take(4).toVector
    val starts = nearby(from)
    val finishes = nearby(to).toSet
    val distances = Array.fill(nodes.size)(Double.PositiveInfinity)
    val previous = Array.fill(nodes.size)(-1)
    case class Entry(index: Int, cost: Double, estimate: Double)
    val queue = mutable.PriorityQueue.empty[Entry](Ordering.by(e => (-e.estimate, -e.index)))
    distances(start) = 0
    queue.enqueue(Entry(start, 0, Math.sqrt(from.tileDistanceSquared(to))))
    var found = false
    while (queue.nonEmpty && !found) {
      val current = queue.dequeue()
      if (current.cost <= distances(current.index)) {
        if (current.index == goal) found = true else {
          val adjacent = if (current.index == start) {
            starts ++ (if (Math.sqrt(from.tileDistanceSquared(to)) <= 8) Vector(goal) else Vector.empty)
          } else {
            val x = current.index % xs.size
            val y = current.index / xs.size
            (for (dy <- -1 to 1; dx <- -1 to 1
                  if (dx != 0 || dy != 0) && x + dx >= 0 && x + dx < xs.size && y + dy >= 0 && y + dy < ys.size)
              yield (x + dx) + (y + dy) * xs.size).toVector ++ (if (finishes(current.index)) Vector(goal) else Vector.empty)
          }
          adjacent.foreach(i => {
            val a = nodes(current.index)
            val b = nodes(i)
            val segment = line(a, b)
            val cost = Math.sqrt(a.tileDistanceSquared(b)) * segment.map(t => 1.0 + exposure(t) + centerCost(t)).sum / segment.size
            val next = current.cost + cost
            if (next < distances(i)) {
              distances(i) = next
              previous(i) = current.index
              queue.enqueue(Entry(i, next, next + Math.sqrt(b.tileDistanceSquared(to))))
            }
          })
        }
      }
    }
    if (!found) return TilePath(from, from, Double.PositiveInfinity, None)
    var indices = List(goal)
    while (indices.head != start) indices = previous(indices.head) :: indices
    val tiles = indices.sliding(2).flatMap(pair => line(nodes(pair.head), nodes(pair.last)).dropRight(1)).toVector :+ to
    TilePath(from, to, distances(goal), Some(tiles))
  }

  def next(from: Tile, to: Tile): Option[TilePath] = {
    if (!destination.contains(to)) {
      destination = Some(to)
      path = None
      progress = None
      failureFrame = None
    }
    if (progress.forall(_.tileDistanceFast(from) >= 2)) {
      progress = Some(from)
      progressFrame = With.frame
    }
    val changed = path.exists(p => p.tiles.get.map(value).toVector != values)
    val offPath = path.exists(_.tiles.get.forall(_.tileDistanceFast(from) > 6))
    if (path.isEmpty || changed || offPath) {
      path = Some(find(from, to)).filter(_.pathExists)
      values = path.map(_.tiles.get.map(value).toVector).getOrElse(Vector.empty)
    }
    if (path.isEmpty || With.framesSince(progressFrame) >= Seconds(8)()) {
      failureFrame = failureFrame.orElse(Some(With.frame))
      None
    } else {
      failureFrame = None
      path
    }
  }

  // Let a speed-upgraded Shuttle accelerate on clear legs. Short (three-tile)
  // orders keep it braking; blindly overshooting them cuts across route hazards.
  def waypoint(from: Pixel, route: TilePath): Pixel = {
    val tiles = route.tiles.get
    val index = tiles.indices.minBy(i => tiles(i).center.pixelDistanceSquared(from))
    val end = Math.min(index + 16, tiles.size - 1)
    (end to index by -1).find(i => {
      val planned = tiles.slice(index, i + 1)
      val direct = line(from.tile, tiles(i))
      direct.map(exposure).sum <= planned.map(exposure).sum &&
        direct.map(centerCost).max <= planned.map(centerCost).max + 0.25
    }).map(tiles(_).center).getOrElse(tiles(Math.min(index + 1, end)).center)
  }
}
