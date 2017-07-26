package Micro.Heuristics.Movement

import Lifecycle.With
import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Points.Pixel
import Micro.Execution.ActionState
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object MovementHeuristicExposureToDamage extends MovementHeuristic {
  
  override def evaluate(state: ActionState, candidate: Pixel): Double = {
    
    val us = state.unit
    
    if (us.effectivelyCloaked
      && ! With.grids.enemyDetection.get(candidate.tileIncluding)
      && us.damageInLastSecond == 0) {
      return HeuristicMathMultiplicative.default
    }
    
    us.matchups.threats.map(threat => evaluateOne(us, threat, candidate)).sum
  }
  
  def evaluateOne(us: FriendlyUnitInfo, threat: UnitInfo, candidate: Pixel): Double = {
    val dpf               = threat.dpfAgainst(us)
    val range             = threat.pixelRangeAgainstFromCenter(us)
    val distance          = threat.pixelDistanceFast(us)
    val travelDelay       = threat.framesToTravelPixels(threat.pixelDistanceFast(candidate)) // Air distance! Ground distance might be interesting too for shooting over cliffs
    val inRangePenalty    = if (range <= distance) 1.0 else 0.0
    val proximityPenalty  = Math.max(0.0, 1.0 - travelDelay / 48.0)
    val output            = dpf * (inRangePenalty + proximityPenalty)
    output
  }
}
