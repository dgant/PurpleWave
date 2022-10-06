package Information.Geography.Pathfinding

import Information.Geography.Pathfinding.Types.{ZonePath, ZonePathNode}
import Information.Geography.Types.{Edge, Zone}
import Lifecycle.With
import Utilities.?

import scala.collection.mutable

trait ZonePathfinder {
  private val paths = new mutable.HashMap[(Zone, Zone, Seq[Edge]), Option[ZonePath]]

  def zonePath(from: Zone, to: Zone, through: Seq[Edge] = Seq.empty): Option[ZonePath] = {
    paths((from, to, through)) = paths.getOrElse((from, to, through), zonePathfind(from, to, through))
    paths((from, to, through))
  }

  // Note that this has the weakness of assuming the path is always from the center of the zone
  // The shortest path from other points of the zone may be different.
  private def zonePathfind(from: Zone, goal: Zone, through: Seq[Edge], pathHere: Vector[ZonePathNode] = Vector.empty): Option[ZonePath] = {
    if (from == goal) {
      return ?(
        through.forall(throughEdge => pathHere.exists(_.edge == throughEdge)),
        Some(ZonePath(pathHere.headOption.map(_.from).getOrElse(goal), goal, pathHere)),
        None)
    }

    val tileFrom  = from.centroid.walkableTile
    val tileTo    = goal.centroid.walkableTile
    if ( ! With.paths.groundPathExists(tileFrom, tileTo)) {
      return None
    }

    val edges = from.edges
      .filterNot(e => pathHere.exists(_.from == e.otherSideof(from)))
      .sortBy(_.distanceGrid.get(tileTo))

    edges.view.map(edge =>
      zonePathfind(
        edge.otherSideof(from),
        goal,
        through,
        pathHere :+ ZonePathNode(from, edge))).find(_.isDefined).flatten
  }
}
