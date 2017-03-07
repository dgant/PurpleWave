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
      val evaluator = new EvaluateApproach(unit.tileCenter, intent.destination)
      val nextStep = EvaluatePositions.bestPosition(unit, evaluator, 2)
      With.commander.move(this, unit, nextStep.centerPixel)
    }
    else {
      With.commander.move(this, unit, intent.destination)
    }
  }
  
  class EvaluateApproach(currentPosition:TilePosition, destination:TilePosition) extends EvaluatePosition {
    override def evaluate(candidate: TilePosition): Double = {
      val distanceBefore = With.paths.groundDistance(currentPosition, destination)
      val distanceAfter = With.paths.groundDistance(candidate, destination)
      val distanceBonus = if (distanceAfter < distanceBefore) 2 else 1
      val mobility = With.grids.mobility.get(candidate)
      val threat = With.grids.enemyGroundStrength.get(candidate).toDouble
      val evaluation =
        if (threat > 0) -threat / (1.0 * mobility * distanceBonus)
        else            distanceBonus
      evaluation
    }
  }
}
