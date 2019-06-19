package Macro.Architecture.PlacementStates

import Lifecycle.With
import Macro.Architecture.ArchitectureDiff
import Macro.Architecture.PlacementRequests.{PlacementRequest, PlacementResult, PlacementTask}
import Mathematics.Points.Tile

import scala.collection.mutable

class PlacementNode(request: PlacementRequest) extends PlacementState {

  case class TileScore(tile: Tile, score: Double = 0.0)

  val queue = new mutable.PriorityQueue[TileScore]()(Ordering.by( - _.score))
  val child: Option[PlacementNode] = request.child.map(new PlacementNode(_))
  val task: PlacementTask = request.task()
  var result: Option[PlacementResult] = None
  var diff: Option[ArchitectureDiff] = None

  var succeeded: Boolean = false
  var failed: Boolean = false
  def done: Boolean = succeeded || failed

  override def step(): Unit = {
    if (queue.isEmpty) {
      if (queue.isEmpty) {
        failed = true
      }
    } else if (result.isEmpty) {
      result = task.step()
      diff = result.filter(_.tile.isDefined).map(With.architecture.diffPlacement)
      diff.foreach(_.doo())
    }
    if (result.isDefined) {
      if (result.get.tile.isEmpty) {
        failed = true
      }
      else if (child.forall(_.succeeded)) {
        succeeded = true
      }
      else if (child.forall(_.failed)) {
        failed = true
      } else {
        child.foreach(_.step())
      }
      if (failed) {
        diff.foreach(_.undo())
      }
      if (done) {
        request.tile = result.get.tile
        request.result = result
        With.placement.finishPlacement(request)
      }
    }
  }
}
