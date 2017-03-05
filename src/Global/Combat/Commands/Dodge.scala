package Global.Combat.Commands

import Global.Combat.Heuristics.{EvaluatePosition, EvaluatePositions}
import Startup.With
import Types.Intents.Intention
import Utilities.Enrichment.EnrichPosition._
import bwapi.TilePosition

object Dodge extends Command {
  
  def execute(intent:Intention) {
    val unit = intent.unit
    val evaluator = new EvaluateDodge(unit.tileCenter, intent.safety)
    val destination = EvaluatePositions.bestPosition(unit, evaluator)
    
    if (unit.onCooldown || unit.distance(destination.centerPosition) > unit.range) {
      With.commander.move(this, unit, destination.centerPosition)
    }
    else {
      Hunt.execute(intent)
    }
  }
  
  class EvaluateDodge (currentPosition:TilePosition, safePosition:TilePosition) extends EvaluatePosition {
    override def evaluate(candidate: TilePosition): Double = {
      val distanceHomeCurrent = With.paths.groundDistance(currentPosition, safePosition)
      val distanceHomeKiting = With.paths.groundDistance(candidate, safePosition)
      val distanceBonus = if (distanceHomeKiting < distanceHomeCurrent) 2 else 1
      val mobility = With.grids.mobility.get(candidate)
      val safety = With.grids.friendlyGroundStrength.get(candidate)
      val threat = With.grids.enemyGroundStrength.get(candidate)
      val evaluation = distanceBonus * mobility * safety / (1.0 + threat * threat)
      evaluation
    }
  }
}

