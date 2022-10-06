package Information.Geography.Pathfinding.Types

import Information.Geography.Types.{Edge, Zone}

case class ZonePathNode(from: Zone, edge: Edge) {
  def to: Zone = edge.otherSideof(from)
}
