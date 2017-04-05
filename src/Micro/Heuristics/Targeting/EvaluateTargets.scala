package Micro.Heuristics.Targeting

import Lifecycle.With
import Micro.Intent.Intention
import ProxyBwapi.UnitInfo.UnitInfo

object EvaluateTargets {
  
  def best(
    intent:Intention,
    profile:TargetingProfile,
    candidates:Iterable[UnitInfo])
      :Option[UnitInfo] = {
    
    if (candidates.isEmpty) return None
    
    if (With.configuration.visualizeHeuristicTargeting) {
      //With.executor.getState(intent.unit).targetHeuristics =
    }
    
    Some(candidates.maxBy(candidate => profile.weightedHeuristics.map(_.weigh(intent, candidate)).product))
  }
}
