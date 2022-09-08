package Information.Geography.Pathfinding

import Information.Geography.Pathfinding.Types.{ZonePath, ZonePathNode}
import Information.Geography.Types.Zone
import Lifecycle.With
import Mathematics.Maff
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

trait ZonePathfinder {

  var pathfindId: Long = Long.MinValue
  private val paths = new mutable.HashMap[(Zone, Zone), Option[ZonePath]]
  def zonePath(from: Zone, to: Zone): Option[ZonePath] = {
    if ( ! paths.contains((from, to))) {
      pathfindId += 1
      paths((from, to)) = zonePathfind(from, to)
    }
    paths((from, to))
  }

  def zonePathUnits(from: Zone, to: Zone): Vector[UnitInfo] = {
    val zones = zonePath(from, to).map(_.zones).getOrElse(Vector(from, to).distinct)
    val output = zones.map(_.units.toVector).fold(Vector.empty)(_ ++ _)
    output
  }

  // Note that this has the weakness of assuming the path is always from the center of the zone
  // The shortest path from other points of the zone may be different.
  protected def zonePathfind(from: Zone, to: Zone, pathHere: Vector[ZonePathNode] = Vector.empty): Option[ZonePath] = {
    from.lastPathfindId = pathfindId
    val startTile = from.centroid.walkableTile
    val endTile = to.centroid.walkableTile
    if ( ! With.paths.groundPathExists(startTile, endTile)) return None
    if (from == to) return Some(Types.ZonePath(from, to, pathHere))

    val bestEdge = Maff.minBy(from.edges.filter(_.otherSideof(from).lastPathfindId != pathfindId))(_.distanceGrid.get(endTile))
    if (bestEdge.isEmpty) return None

    val nextZone = bestEdge.get.otherSideof(from)
    zonePathfind(nextZone, to, pathHere :+ ZonePathNode(from, bestEdge.get))
  }
}
