package Macro.Architecture.Heuristics
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile

object PlacementHeuristicRhythm extends PlacementHeuristic {

  def scoreOffset(offset: Int): Int = {
    if (offset == 0) 2 else 1
  }

  override def evaluate(building: Blueprint, candidate: Tile): Double = {
    val origin = candidate.base.map(_.townHallTile).getOrElse(candidate.zone.centroid)
    val offsetX = Math.abs(candidate.x % 5 - origin.x % 5)
    val offsetY = Math.abs(candidate.y % 4 - origin.y % 4)
    val output = scoreOffset(offsetX) + scoreOffset(offsetY)
    output
  }
}
