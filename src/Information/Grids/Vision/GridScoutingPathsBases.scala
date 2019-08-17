package Information.Grids.Vision

import Information.Grids.Movement.GridGroundDistance
import Lifecycle.With
import Mathematics.Points.Tile

class GridScoutingPathsBases extends GridGroundDistance {

  override def origins: Vector[Tile] =
    With.geography.bases.map(_.townHallTile).flatMap(a =>
      With.geography.bases.map(_.townHallTile).filterNot(_ == a).flatMap(
        With.paths.profileDistance(a, _).find.tiles))
      .flatten
      .distinct
}
