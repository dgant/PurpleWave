package Information.Geography.Pathfinding

import Information.Geography.Types.{Edge, Zone}

case class ZonePathNode(
  from : Zone,
  to   : Zone,
  edge : Edge) {
  
  lazy val length: Double = edge.zones.map(zone => edge.centerPixel.pixelDistanceFast(zone.centroid.pixelCenter)).sum
}
