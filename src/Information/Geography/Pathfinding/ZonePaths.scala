package Information.Geography.Pathfinding

import Information.Geography.Pathfinding.Types.{ZonePath, ZonePathNode}
import Information.Geography.Types.Zone
import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.ByOption

import scala.collection.mutable

trait ZonePaths {

  private val paths = new mutable.HashMap[(Zone, Zone), Option[ZonePath]]
  def zonePath(from: Zone, to: Zone): Option[ZonePath] = {
    if (! paths.contains((from, to))) {
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
  protected def zonePathfind(
    from: Zone,
    to: Zone,
    pathHere: Vector[ZonePathNode] = Vector.empty)
      : Option[ZonePath] = {

    val start = from.centroid
    val end = to.centroid
    if (!With.paths.groundPathExists(start, end)) return None

    if (from == to) return Some(Types.ZonePath(from, to, pathHere))
    val bestEdge = ByOption.minBy(from.edges)(_.distanceGrid.get(end))
    if (bestEdge.isEmpty) return None // Defensive; not sure why this would ever happen

    zonePathfind(bestEdge.get.otherSideof(from), to, pathHere :+ ZonePathNode(from, to, bestEdge.get))
  }
}
