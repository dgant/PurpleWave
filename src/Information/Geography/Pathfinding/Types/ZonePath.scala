package Information.Geography.Pathfinding.Types

import Information.Geography.Types.Zone

case class ZonePath(
  from  : Zone,
  to    : Zone,
  steps : Vector[ZonePathNode]) {
  lazy val zones: Vector[Zone] = Vector(from) ++ steps.map(_.to)
}
