package Global.Combat.Heuristics

import bwapi.TilePosition

trait EvaluatePosition {
  def evaluate(candidate:TilePosition):Double
}
