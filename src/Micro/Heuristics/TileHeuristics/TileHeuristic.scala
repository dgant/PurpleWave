package Micro.Heuristics.TileHeuristics

import Micro.Intentions.Intention
import bwapi.TilePosition

trait TileHeuristic {
  def evaluate(intent: Intention, candidate: TilePosition):Double
}
