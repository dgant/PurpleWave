package Micro.Heuristics.TileHeuristics

import Micro.Intent.Intention
import bwapi.TilePosition

trait TileHeuristic {
  
  def evaluate(intent: Intention, candidate: TilePosition):Double
  
}
