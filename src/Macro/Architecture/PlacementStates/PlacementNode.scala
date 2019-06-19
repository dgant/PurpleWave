package Macro.Architecture.PlacementStates

import Lifecycle.With
import Macro.Architecture.ArchitectureDiff
import Macro.Architecture.PlacementRequests.{PlacementRequest, PlacementResult, PlacementTask}
import Mathematics.Points.Tile
import Utilities.ByOption

import scala.collection.mutable

class PlacementNode(request: PlacementRequest) extends PlacementState {

  case class TileScore(tile: Tile, score: Double = 0.0)

  def makeChild: Option[PlacementNode] = request.child.map(new PlacementNode(_))

  val task: PlacementTask = request.task()
  val tileQueue = new mutable.PriorityQueue[TileScore]()(Ordering.by( - _.score))
  var needToLoadTiles: Boolean = true
  var child: Option[PlacementNode] = makeChild
  var tile: Option[Tile] = None
  var diff: Option[ArchitectureDiff] = None

  var succeeded: Boolean = false
  var failed: Boolean = false
  def done: Boolean = succeeded || failed

  // Diagnostics
  val result = PlacementResult(request)

  def doDiff(newDiff: ArchitectureDiff): Unit = {
    diff = Some(newDiff)
    newDiff.doo()
  }

  def undoDiff(): Unit = {
    diff.foreach(_.undo())
    diff = None
  }

  override def step(): Unit = {
    if (needToLoadTiles) {
      needToLoadTiles = false

      // An inelegant design here:
      // If we don't need to do recursive placement (ie. we have no children to place)
      // then short-circuit the (slow) priority queue by giving it only the winning candidate.
      val allTiles = task.tiles.filter(task.accept)
      result.candidates = allTiles.size
      if (allTiles.isEmpty) {
        failed = true
        return
      }
      if (child.isEmpty) {
        tile = ByOption.minBy(allTiles.map(t => (t, task.score(t))))(_._2).map(_._1)
      } else {
        tileQueue ++= allTiles.map(tile => TileScore(tile, task.score(tile)))
      }
      return
    }
    if (child.exists(_.failed)) {
      tile = None
      child = makeChild
      undoDiff()
    }
    while (tile.isEmpty && tileQueue.nonEmpty) {
      result.evaluated += 1
      tile = Some(tileQueue.dequeue().tile).filter(task.accept)
      tile.map(With.architecture.diffPlacement(_, request)).foreach(doDiff)
    }
    if (tile.isEmpty) {
      failed = true
      return
    }
    if (child.exists( ! _.done)) {
      child.foreach(_.step())
    } else {
      succeeded = true
    }

    if (failed) {
      diff.foreach(_.undo())
    }
    if (done) {
      result.frameFinished = With.frame
      request.tile = tile
      request.result = Some(result)
    }
  }
}
