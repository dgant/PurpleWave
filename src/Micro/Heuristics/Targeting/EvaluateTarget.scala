package Micro.Heuristics.Targeting

import Micro.Intent.Intention
import ProxyBwapi.UnitInfo.UnitInfo

trait EvaluateTarget {
  def evaluate(intent:Intention, target:UnitInfo):Double
}
