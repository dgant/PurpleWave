package Global.Combat.Commands

import Global.Combat.Heuristics.{EvaluatePosition, EvaluatePositions}
import Startup.With
import Types.Intents.Intention
import Types.UnitInfo.FriendlyUnitInfo
import Utilities.Enrichment.EnrichPosition._
import bwapi.TilePosition

object Dodge extends Command {
  
  def execute(intent:Intention) {
    val unit = intent.unit
    val evaluator = new EvaluateDodge(unit, intent.safety)
    val destination = EvaluatePositions.bestPosition(unit, evaluator, 3)
    
    if (unit.onCooldown || unit.distance(destination.centerPixel) > unit.range) {
      With.commander.move(this, unit, destination.centerPixel)
    }
    else {
      Hunt.execute(intent)
    }
  }
  
  class EvaluateDodge (unit:FriendlyUnitInfo, safePosition:TilePosition) extends EvaluatePosition {
    override def evaluate(candidate: TilePosition): Double = {
      val currentPosition = unit.tileCenter
      val distanceBefore = With.paths.groundDistance(currentPosition, safePosition)
      val distanceAfter = With.paths.groundDistance(candidate, safePosition)
      val distanceBonus = if (distanceAfter < distanceBefore) 2 else 1
      val stepSizePenalty = candidate.tileDistance(currentPosition)
      val mobilityBonus = With.grids.mobility.get(candidate)
      val safetyBonus = With.grids.friendlyGroundStrength.get(candidate)
      val threatPenalty = With.grids.enemyGroundStrength.get(candidate)
      val trafficPenalty = (With.grids.units.get(candidate) -- List(unit)).size
      
      val evaluation =
        distanceBonus * mobilityBonus * safetyBonus /
        ((1.0 + trafficPenalty) * threatPenalty * threatPenalty * stepSizePenalty)
      evaluation
    }
  }
}

