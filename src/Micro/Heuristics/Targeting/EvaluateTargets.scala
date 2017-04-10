package Micro.Heuristics.Targeting

import Lifecycle.With
import Micro.Intent.Intention
import ProxyBwapi.UnitInfo.UnitInfo

object EvaluateTargets {
  
  def best(intent:Intention, candidates:Iterable[UnitInfo]):Option[UnitInfo] = {
    
    if (candidates.isEmpty) return None
    
    if (With.configuration.visualizeHeuristicTargeting) {
      //With.executor.getState(intent.unit).targetHeuristics =
    }
    
    Some(candidates.maxBy(candidate => intent.targetProfile.weightedHeuristics.map(_.weigh(intent, candidate)).product))
  }
}
