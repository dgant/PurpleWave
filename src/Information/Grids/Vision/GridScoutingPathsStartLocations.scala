package Information.Grids.Vision

import Information.Grids.Movement.GridGroundDistance
import Lifecycle.With
import Mathematics.Points.Tile

class GridScoutingPathsStartLocations extends GridGroundDistance {

  override def origins: Vector[Tile] =
    With.geography.bases.filter(_.isStartLocation).map(_.heart).flatMap(a =>
      With.geography.bases.filter(_.isStartLocation).map(_.heart).filterNot(a==)
        .flatMap(With.paths.profileDistance(a, _).find.tiles))
      .flatten
      .distinct
}
