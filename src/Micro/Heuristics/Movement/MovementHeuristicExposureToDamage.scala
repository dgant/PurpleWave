package Micro.Heuristics.Movement

import Mathematics.Points.Pixel
import Micro.Execution.ActionState
import ProxyBwapi.UnitInfo.UnitInfo

object MovementHeuristicExposureToDamage extends MovementHeuristic {
  
  override def evaluate(state: ActionState, candidate: Pixel): Double = {
  
    if (state.threats.forall(_.melee)) return MovementHeuristicThreatDistance.evaluate(state, candidate)
    
    // DPS grid's resolution is too low. It leads to wobby, wasteful movement.
    // With.grids.dpsEnemy.get(candidate.tileIncluding, state.unit)
    state.threats.take(20).map(threat => evaluateOne(state, threat, candidate)).sum
  }
  
  def evaluateOne(state: ActionState, threat: UnitInfo, candidate: Pixel): Double = {
    val dps               = threat.dpsAgainst(state.unit)
    val range             = threat.pixelRangeAgainstFromCenter(state.unit)
    val distance          = threat.pixelDistanceFast(state.unit)
    val travelDelay       = threat.framesToTravelPixels(threat.pixelDistanceFast(candidate)) // Air distance! Ground distance might be interesting too for shooting over cliffs
    val violencePenalty   = if (threat.isBeingViolentTo(state.unit)) 1.0 else 0.0
    val inRangePenalty    = if (range <= distance) 1.0 else 0.0
    val proximityPenalty  = Math.max(0.0, 1.0 - travelDelay/48.0)
    val output            = dps * (violencePenalty + inRangePenalty + proximityPenalty)
    output
  }
}
