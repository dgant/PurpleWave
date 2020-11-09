package Information.Geography.Pathfinding.Types

import Information.Geography.Types.{Edge, Zone}

case class ZonePathNode(from: Zone, edge: Edge) {
  val to: Zone = edge.otherSideof(from)
  lazy val lengthPixels: Double = edge.zones.map(zone => edge.pixelCenter.pixelDistance(zone.centroid.pixelCenter)).sum
}
