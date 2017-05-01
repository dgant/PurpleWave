package Micro.Heuristics.MovementHeuristics

import Lifecycle.With
import Mathematics.Pixels.Pixel
import Micro.Intent.Intention

object MovementHeuristicExposureToDamage extends MovementHeuristic {
  
  override def evaluate(intent: Intention, candidate: Pixel): Double = {
  
    With.grids.dpsEnemy.get(candidate.tileIncluding, intent.unit)
    
  }
}
