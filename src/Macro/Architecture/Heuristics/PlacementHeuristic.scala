package Macro.Architecture.Heuristics

import Macro.Architecture.Blueprint
import Placement.Heuristics.Heuristic
import Mathematics.Points.Tile

abstract class PlacementHeuristic extends Heuristic[Blueprint, Tile] {
  
  def evaluate(building: Blueprint, candidate: Tile): Double
  
}
