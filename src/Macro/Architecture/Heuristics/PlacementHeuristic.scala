package Macro.Architecture.Heuristics

import Macro.Architecture.BuildingDescriptor
import Mathematics.Heuristics.Heuristic
import Mathematics.Points.Tile

abstract class PlacementHeuristic extends Heuristic[BuildingDescriptor, Tile] {
  
  def evaluate(building: BuildingDescriptor, candidate: Tile): Double
  
}
