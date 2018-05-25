package Macro.Architecture.Heuristics

import Macro.Architecture.Blueprint
import Mathematics.Points.Tile

object PlacementHeuristicZone extends PlacementHeuristic {
  
  override def evaluate(blueprint: Blueprint, candidate: Tile): Double = {
    
    val candidateZone = candidate.zone
    val candidateBase = candidate.base
  
    val baseMatches = candidateBase.exists(_.owner.isUs) &&
      (blueprint.preferZone.isEmpty || candidateBase.exists(_.zone == blueprint.preferZone.get))
    
    val zoneMatches =
      if (blueprint.preferZone.isDefined)
        blueprint.preferZone.contains(candidateZone)
      else
        candidateZone.owner.isFriendly
    
    val output =
      if (zoneMatches && baseMatches)
        2.0
      else if(zoneMatches || baseMatches)
        1.5
      else
        1.0
    
    output
  }
}
