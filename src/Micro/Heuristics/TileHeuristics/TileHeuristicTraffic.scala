package Micro.Heuristics.TileHeuristics

import Micro.Intentions.Intention
import Startup.With
import bwapi.TilePosition

object TileHeuristicTraffic extends TileHeuristic {
  
  override def evaluate(intent: Intention, candidate: TilePosition): Double = {
  
    if (intent.unit.flying) 1 else
      With.grids.units.get(candidate)
        .filterNot(_ == intent.unit)
        .filterNot(_.flying)
        .map(unit => unit.unitClass.width * unit.unitClass.height)
        .sum
    
  }
  
}
