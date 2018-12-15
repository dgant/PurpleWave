package Information.Grids.Vision

import Information.Grids.Movement.GridGroundDistance
import Lifecycle.With
import Mathematics.Points.Tile

class GridScoutingPathsStartLocations extends GridGroundDistance {

  override def origins: Vector[Tile] =
    With.geography.startLocations.flatMap(a =>
      With.geography.startLocations.filterNot(_ == a).flatMap(
        With.paths.aStarBasic(a, _).tiles))
      .flatten
      .distinct
}
