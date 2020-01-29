package Macro.Architecture.Heuristics
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile

object PlacementHeuristicRhythm extends PlacementHeuristic {

  def scoreOffset(offset: Int): Int = {
    if (offset == 0) 2 else 1
  }

  val gridW = 7
  val gridH = 6
  override def evaluate(building: Blueprint, candidate: Tile): Double = {
    val origin = candidate.base.map(_.townHallTile).getOrElse(candidate.zone.centroid)
    val x = candidate.x
    val y = candidate.y
    val originX = origin.x % gridW
    val originY = origin.y % gridH
    val offsetX = building.rhythmsX.map(rx => Math.abs(x - rx - originX)).min
    val offsetY = building.rhythmsY.map(ry => Math.abs(y - ry - originY)).min
    val output = scoreOffset(offsetX) + scoreOffset(offsetY)
    output
  }
}
