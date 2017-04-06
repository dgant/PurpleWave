package Micro.Heuristics.MovementHeuristics

import Micro.Intent.Intention
import Lifecycle.With
import bwapi.TilePosition

object MovementHeuristicExposureToDamage extends MovementHeuristic {
  
  override def evaluate(intent: Intention, candidate: TilePosition): Double = {
  
    Math.min(200.0, Math.max(1.0, With.grids.dpsEnemy.get(candidate, intent.unit)))
    
  }
  
}
