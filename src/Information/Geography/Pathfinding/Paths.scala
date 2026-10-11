package Information.Geography.Pathfinding

import Lifecycle.With

final class Paths extends TilePathfinder with ZonePathfinder with GroundDistance with GroundTopology {
  lazy val broodWarGroundDistance: BroodWarGroundDistance = BroodWarGroundDistance(With.game)
}