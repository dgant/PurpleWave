package Macro.Architecture.PlacementStates

import Lifecycle.With
import Macro.Architecture.ArchitectureDiff
import Macro.Architecture.PlacementRequests.{PlacementRequest, PlacementPolicy}
import Mathematics.Points.Tile
import Utilities.ByOption

import scala.collection.mutable

class PlacementNode(request: PlacementRequest) extends PlacementState {

  case class TileScore(tile: Tile, score: Double = 0.0)

  def makeChild: Option[PlacementNode] = request.child.map(new PlacementNode(_))

  val task: PlacementPolicy = request.policy()
  val tileQueue = new mutable.PriorityQueue[TileScore]()(Ordering.by( - _.score))
  var needToLoadTiles: Boolean = true
  var child: Option[PlacementNode] = makeChild
  var tile: Option[Tile] = None
  var diff: Option[ArchitectureDiff] = None

  var succeeded: Boolean = false
  var failed: Boolean = false
  def done: Boolean = succeeded || failed

  def doDiff(newDiff: ArchitectureDiff): Unit = {
    diff = Some(newDiff)
    newDiff.doo()
  }

  def undoDiff(): Unit = {
    diff.foreach(_.undo())
    diff = None
  }

  def fail(): Unit = {
    failed = true
    request.tile = None
    request.lastPlacementFrame = With.frame
    //With.logger.warnCircuitBreaker("Failed to place " + request)
  }

  def succeed(tile: Tile): Unit = {
    succeeded = true
    request.tile = Some(tile)
    request.lastPlacementFrame = With.frame
  }

  override def step(): Unit = {
    if (needToLoadTiles) {
      needToLoadTiles = false

      // An inelegant design here:
      // If we don't need to do recursive placement (ie. we have no children to place)
      // then short-circuit the (slow) priority queue by giving it only the winning candidate.
      var allTiles: Seq[Tile] = Seq.empty
      task.tiles.find(tiles => {
        allTiles = tiles.filter(task.accept)
        allTiles.nonEmpty
      })

      if (allTiles.isEmpty) {
        fail()
        return
      }

      if (child.isEmpty) {
        tileQueue ++= ByOption.minBy(allTiles.map(t => TileScore(t, task.score(t))))(_.score)
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
      tile = Some(tileQueue.dequeue().tile).filter(task.accept)
      tile.map(With.architecture.diffPlacement(_, request)).foreach(doDiff)
    }
    if (tile.isEmpty) {
      fail()
      return
    }
    if (child.exists( ! _.done)) {
      child.foreach(_.step())
    } else {
      succeeded = true
    }

    if (succeeded) {
      succeed(tile.get)
    } else if (failed) {
      diff.foreach(_.undo())
    }
  }
}
