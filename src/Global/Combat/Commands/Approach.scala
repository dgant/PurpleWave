package Global.Combat.Commands

import Global.Combat.Heuristics.{EvaluatePosition, EvaluatePositions}
import Startup.With
import Types.Intents.Intention
import bwapi.TilePosition
import Utilities.Enrichment.EnrichPosition._

object Approach extends Command {
  
  def execute(intent:Intention) {
    val unit = intent.unit
    val evaluator = new EvaluateApproach(unit.tilePosition, intent.destination)
    val nextStep = EvaluatePositions.bestPosition(unit, evaluator)
    With.commander.move(this, unit, nextStep.centerPosition)
  }
  
  class EvaluateApproach(currentPosition:TilePosition, destination:TilePosition) extends EvaluatePosition {
    override def evaluate(candidate: TilePosition): Double = {
      val distanceNow = With.paths.groundDistance(currentPosition, destination)
      val distanceCandidate = With.paths.groundDistance(candidate, destination)
      val distanceBonus = distanceNow - distanceCandidate
      val mobility = With.grids.mobility.get(candidate)
      val threat = With.grids.enemyGroundStrength.get(candidate)
      val evaluation = if (threat > 0) -threat / (1.0 + mobility) else distanceBonus
      evaluation
    }
  }
}
