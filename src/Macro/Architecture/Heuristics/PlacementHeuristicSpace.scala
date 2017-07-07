package Macro.Architecture.Heuristics

import Macro.Architecture.Blueprint
import Mathematics.Points.Tile

object PlacementHeuristicSpace extends PlacementHeuristic {
  
  override def evaluate(blueprint: Blueprint, candidate: Tile): Double = {
    
    candidate.zone.area / 10000.0
    
  }
}
