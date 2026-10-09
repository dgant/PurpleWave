package Tactic.Missions

import Information.Geography.Pathfinding.Types.TilePath
import Information.Geography.Pathfinding.{PathfindProfile, PathfindRepulsor}
import Lifecycle.With
import Mathematics.Points.{Points, Tile}
import Utilities.Time.Seconds

import scala.collection.mutable

private[Missions] class DropRoute {
  private var destination: Option[Tile] = None
  private var path: Option[TilePath] = None
  private var values = Vector.empty[(Int, Boolean)]
  private val visited = mutable.Set.empty[Tile]
  private var progress: Option[Tile] = None
  private var progressFrame = With.frame
  private var failureFrame: Option[Int] = None
  private var retryFrame = Int.MinValue

  def stalled: Boolean = failureFrame.exists(With.framesSince(_) >= Seconds(8)())
  private def value(tile: Tile): (Int, Boolean) = (With.grids.enemyRangeAir.get(tile), tile.visibleToEnemy)

  def next(from: Tile, to: Tile): Option[TilePath] = {
    if (!destination.contains(to)) {
      destination = Some(to)
      path = None
      visited.clear()
      progress = None
      failureFrame = None
      retryFrame = With.frame
    }
    if (progress.forall(_.tileDistanceFast(from) >= 2)) {
      progress = Some(from)
      progressFrame = With.frame
    }
    val changed = path.exists(p => p.tiles.get.map(value).toVector != values)
    val reached = path.exists(p => p.end != to && p.end.tileDistanceFast(from) <= 3)
    val offPath = path.exists(_.tiles.get.forall(_.tileDistanceFast(from) > 6))
    if (changed || reached || offPath || (path.isEmpty && With.frame >= retryFrame)) {
      if (changed) visited.clear()
      if (reached) path.foreach(p => visited += p.end)
      val margin = Math.min(With.mapTileWidth, With.mapTileHeight) - Math.max(3,
        Seq(to.x, to.y, With.mapTileWidth - to.x, With.mapTileHeight - to.y).min)
      // Full-map searches exhaust the A* budget. Follow short partial paths and
      // replan near their ends, retaining air-threat and vision costs on each leg.
      val profile = new PathfindProfile(from, Some(to),
        lengthMaximum = Some(24), acceptPartialPath = true,
        canCrossUnwalkable = Some(true), canEndUnwalkable = Some(true),
        costEnemyVision = 2.5, costThreat = 10.0, costRepulsion = 0.1,
        repulsors = Vector(PathfindRepulsor(Points.middle, 1.0, 32 * margin)))
      path = Some(profile.find).filter(p => p.pathExists && (p.end == to || (p.length > 1 && !visited(p.end))))
      values = path.map(_.tiles.get.map(value).toVector).getOrElse(Vector.empty)
      retryFrame = With.frame + Seconds(2)()
    }
    if (path.isEmpty || With.framesSince(progressFrame) >= Seconds(8)()) {
      failureFrame = failureFrame.orElse(Some(With.frame))
      None
    } else {
      failureFrame = None
      path
    }
  }
}
