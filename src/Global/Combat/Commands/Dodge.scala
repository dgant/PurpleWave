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
    val destination = EvaluatePositions.bestPosition(unit, evaluator, 3)
    
    if (unit.onCooldown || unit.distance(destination.centerPixel) > unit.range) {
      With.commander.move(this, unit, destination.centerPixel)
    }
    else {
      Hunt.execute(intent)
    }
  }
  
  class EvaluateDodge (currentPosition:TilePosition, safePosition:TilePosition) extends EvaluatePosition {
    override def evaluate(candidate: TilePosition): Double = {
      val distanceBefore = With.paths.groundDistance(currentPosition, safePosition)
      val distanceAfter = With.paths.groundDistance(candidate, safePosition)
      val distanceBonus = if (distanceAfter < distanceBefore) 2 else 1
      val mobility = With.grids.mobility.get(candidate)
      val safety = With.grids.friendlyGroundStrength.get(candidate)
      val threat = With.grids.enemyGroundStrength.get(candidate)
      val traffic = With.grids.units.get(candidate).size
      
      val evaluation =
        distanceBonus * mobility * safety /
        ((1.0 + traffic) * + threat * threat )
      evaluation
    }
  }
}

