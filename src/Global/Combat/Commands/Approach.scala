package Global.Combat.Commands

import Global.Combat.Heuristics.{EvaluatePosition, EvaluatePositions}
import Startup.With
import Types.Intents.Intention
import bwapi.TilePosition
import Utilities.Enrichment.EnrichPosition._

object Approach extends Command {
  
  def execute(intent:Intention) {
    val unit = intent.unit
    val rangeToPath = 16 * 32
    if (intent.battle.exists(_.enemy.units.exists(_.distanceSquared(unit) < rangeToPath * rangeToPath))) {
      val evaluator = new EvaluateApproach(unit.tileCenter, intent)
      val nextStep = EvaluatePositions.bestPosition(unit, evaluator, 2)
      With.commander.move(this, unit, nextStep.centerPixel)
    }
    else {
      With.commander.move(this, unit, intent.destination)
    }
  }
  
  class EvaluateApproach(currentPosition:TilePosition, intent:Intention) extends EvaluatePosition {
    override def evaluate(candidate: TilePosition): Double = {
      val threat = With.grids.enemyGroundStrength.get(candidate).toDouble
      val destination = if(threat > 0) intent.safety else intent.destination
      val distanceBefore = With.paths.groundDistance(currentPosition, destination)
      val distanceAfter = With.paths.groundDistance(candidate, destination)
      val distanceFactor = if (distanceAfter < distanceBefore) 2 else 1
      val traffic = 1 + (With.grids.units.get(candidate) -- List(intent.unit)).size //Maybe double-count enemies; maybe account for density
      val evaluation =
        if (threat > 0) -threat * traffic / distanceFactor
        else distanceFactor
      evaluation
    }
  }
}
