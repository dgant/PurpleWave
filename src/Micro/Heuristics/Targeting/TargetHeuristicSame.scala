package Micro.Heuristics.Targeting
import Mathematics.Heuristics.HeuristicMath
import Micro.Intent.Intention
import ProxyBwapi.UnitInfo.UnitInfo

object TargetHeuristicSame extends TargetHeuristic {
  
  override def evaluate(intent: Intention, candidate: UnitInfo): Double = {
  
    HeuristicMath.fromBoolean(intent.state.target.exists(_ == candidate))
    
  }
  
}
