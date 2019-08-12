package Information.Geography.Pathfinding

import Information.Geography.Pathfinding.Types.TilePath
import Lifecycle.With
import Mathematics.Points.Tile

final case class PathfindProfile(
                                  start: Tile,
                                  end: Option[Tile] = None,
                                  goalDistance: Option[Int] = None,
                                  maximumLength: Option[Float] = None,
                                  flying: Boolean = false,
                                  costOccupancy: Float = 0f,
                                  costThreat: Float = 0f,
                                  costEnemyVision: Float = 0f) {
  def find: TilePath = With.paths.aStar(this)
}