package Information.Geography.Pathfinding

import Information.Geography.Types.Zone
import Utilities.ByOption

object ZonePathfinder {
  
  // Note that this has the weakness of assuming the path is always from the center of the zone
  // The shortest path from other points of the zone may be different.
  def find(
    from      : Zone,
    to        : Zone,
    pathHere  : Vector[ZonePathNode]  = Vector.empty,
    explored  : Set[Zone]             = Set.empty)
      : Option[ZonePath] = {
    
    if (from == to) {
      return Some(ZonePath(
        from  = from,
        to    = to,
        steps = pathHere))
    }
    val edges = from.edges
      .filterNot(edge => explored.contains(edge.otherSideof(from)))
      .sortBy(_.centerPixel.pixelDistanceFast(from.centroid.pixelCenter))
    
    val paths = edges.flatMap(edge => {
      val nextZone = edge.otherSideof(from)
      find(
        nextZone,
        to,
        pathHere :+ ZonePathNode(from, nextZone, edge),
        explored + from)
    })
    
    ByOption.minBy(paths)(_.lengthPixels)
  }
}
