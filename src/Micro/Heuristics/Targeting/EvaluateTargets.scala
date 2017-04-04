package Micro.Heuristics.Targeting

import Micro.Intent.Intention
import ProxyBwapi.UnitInfo.UnitInfo

object EvaluateTargets {
  
  def best(
    intent:Intention,
    profile:TargetingProfile,
    candidates:Iterable[UnitInfo])
      :Option[UnitInfo] = {
    
    if (candidates.isEmpty) return None
    
    //TODO: Compare best candidate to "how about we just don't attack, at all?"
    
    Some(candidates.maxBy(candidate => profile.weightedHeuristics.map(_.weigh(intent, candidate)).product))
  }
}
