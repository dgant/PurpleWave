package Information.Geography.Pathfinding

import Lifecycle.With
import Mathematics.Points.Tile

final class TileState(val tile: Tile) {
  val i: Int = tile.i
  var _visitedStamp   : Long  = With.paths.stampDefault
  var _enqueuedStamp  : Long  = With.paths.stampDefault
  var _cameFrom       : Tile  = _
  var _costFromStart  : Double = _
  var _costToEndFloor : Double = _
  var _pathLength     : Double = _
  var _repulsion      : Double = _
  @inline def setEnqueued() {
    _enqueuedStamp = With.paths.stampCurrent
  }
  @inline def setVisited() {
    _visitedStamp = With.paths.stampCurrent
  }
  @inline def setCameFrom(value: Tile) {
    _cameFrom = value
  }
  // Cost of best-known path from the start tile.
  // In common A* parlance, this is the gScore.
  @inline def setCostFromStart(value: Double) {
    _costFromStart = value
  }
  // Minimum possible cost to the end.
  // In common A* parlance, this is the fScore.
  @inline def setTotalCostFloor(value: Double) {
    _costToEndFloor = value
  }
  @inline def setPathLength(value: Double) {
    _pathLength = value
  }
  @inline def setRepulsion(value: Double): Unit = {
    _repulsion = value
  }
  @inline def enqueued        : Boolean       = _enqueuedStamp == With.paths.stampCurrent
  @inline def visited         : Boolean       = _visitedStamp == With.paths.stampCurrent
  @inline def cameFrom        : Option[Tile]  = if (enqueued && _cameFrom.i != i) Some(_cameFrom) else None
  @inline def costFromStart   : Double        = _costFromStart
  @inline def totalCostFloor  : Double        = _costToEndFloor
  @inline def pathLength      : Double        = _pathLength
  @inline def repulsion       : Double        = _repulsion
}