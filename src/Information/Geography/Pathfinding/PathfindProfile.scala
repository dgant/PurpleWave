package Information.Geography.Pathfinding

import Information.Geography.Pathfinding.Types.TilePath
import Lifecycle.With
import Mathematics.Points.Tile

case class PathfindProfile(
  start: Tile,
  end: Option[Tile] = None,
  goalDistance: Option[Int] = None,
  maximumCost: Option[Float] = None,
  flying: Boolean = false,
  costDistanceAway: Float = 1f,
  costDistanceHome: Float = 1f,
  costOccupancy: Float = 0f,
  costThreat: Float = 0f,
  costEnemyVision: Float = 0f) {
  def find: TilePath = With.paths.aStar(this)
}