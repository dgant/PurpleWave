package Micro.Heuristics.TileHeuristics

import Micro.Intent.Intention
import Lifecycle.With
import bwapi.TilePosition

object TileHeuristicExposureToDamage extends TileHeuristic {
  
  override def evaluate(intent: Intention, candidate: TilePosition): Double = {
  
    Math.max(1.0, With.grids.dpsEnemy.get(candidate, intent.unit.unitClass) / 100.0)
    
  }
  
}
