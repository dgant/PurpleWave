package Micro.Heuristics.MovementHeuristics

import Lifecycle.With
import Mathematics.Pixels.Pixel
import Micro.Intent.Intention

object MovementHeuristicExposureToDamage extends MovementHeuristic {
  
  override def evaluate(intent: Intention, candidate: Pixel): Double = {
  
    Math.min(200.0, Math.max(1.0, With.grids.dpsEnemy.get(candidate.tileIncluding, intent.unit)))
    
  }
  
}
