package Micro.Heuristics.Targeting

import Micro.Intentions.Intention
import ProxyBwapi.UnitInfo.UnitInfo

object EvaluateTargets {
  
  def best(
    intent:Intention,
    evaluator:EvaluateTarget,
    targets:Iterable[UnitInfo])
      :Option[UnitInfo] = {
    
    if (targets.isEmpty) return None
    
    Some(targets.maxBy(target => evaluator.evaluate(intent, target)))
  }
}
