package Micro.Heuristics.Movement

import Micro.Intent.Intention
import bwapi.TilePosition

trait EvaluatePosition {
  def evaluate(intent:Intention, candidate:TilePosition):Double
}
