package Global.Combat.Commands

import Global.Combat.Heuristics.{EvaluatePosition, EvaluatePositions}
import Startup.With
import Types.Intents.Intention
import bwapi.TilePosition
import Utilities.Enrichment.EnrichPosition._

object Approach extends Command {
  
  def execute(intent:Intention) {
    val unit = intent.unit
    if (intent.battle.nonEmpty && intent.battle.get.enemy.units.exists(_.distanceSquared(unit) < 15 * 15 * 32 * 32)) {
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
      val distanceBonus = distanceBefore - distanceAfter
      val mobility = With.grids.mobility.get(candidate)
      val threat = With.grids.enemyGroundStrength.get(candidate)
      val evaluation =
        if (threat > 0) -threat / mobility
        else            distanceBonus
      evaluation
    }
  }
}
