package Micro.Movement

import Micro.Intentions.Intention
import bwapi.TilePosition

trait EvaluatePosition {
  def evaluate(intent:Intention, candidate:TilePosition):Double
}
