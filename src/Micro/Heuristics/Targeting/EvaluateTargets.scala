package Micro.Heuristics.Targeting

import Micro.Intent.Intention
import ProxyBwapi.UnitInfo.UnitInfo

object EvaluateTargets {
  
  def best(intent:Intention, candidates:Iterable[UnitInfo]):Option[UnitInfo] = {
    
    if (candidates.isEmpty) return None
    
    Some(candidates.maxBy(candidate => intent.targetProfile.weightedHeuristics.map(_.weigh(intent, candidate)).product))
  }
}
