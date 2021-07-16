package Information.Geography.Pathfinding.Types

import Mathematics.Points.Tile

object NoPath {
  val value = TilePath(Tile(0, 0), Tile(0, 0), 0, None)
}

