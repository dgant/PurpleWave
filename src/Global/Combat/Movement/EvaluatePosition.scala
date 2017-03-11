package Global.Combat.Movement

import Types.Intents.Intention
import bwapi.TilePosition

trait EvaluatePosition {
  def evaluate(intent:Intention, candidate:TilePosition):Double
}
