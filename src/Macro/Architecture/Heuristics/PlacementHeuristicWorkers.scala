package Macro.Architecture.Heuristics

import Macro.Architecture.Blueprint
import Mathematics.Points.Tile

object PlacementHeuristicWorkers extends PlacementHeuristic {
  
  override def evaluate(blueprint: Blueprint, candidate: Tile): Double = {
    1 + candidate.base.map(_.workerCount).sum
  }
}
