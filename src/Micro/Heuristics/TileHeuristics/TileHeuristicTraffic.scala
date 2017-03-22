package Micro.Heuristics.TileHeuristics

import Micro.Intentions.Intention
import Startup.With
import bwapi.TilePosition

object TileHeuristicTraffic extends TileHeuristic {
  
  val scaling = 1.0 / 32.0 / 32.0
  
  override def evaluate(intent: Intention, candidate: TilePosition): Double = {
  
    if (intent.unit.flying) 1.0 else
      1.0 + scaling *
      With.grids.units.get(candidate)
        .filterNot(_ == intent.unit)
        .filterNot(_.flying)
        .map(unit =>
          Math.max(32.0, unit.unitClass.width) *
          Math.max(32.0, unit.unitClass.height))
        .sum
  }
  
}
