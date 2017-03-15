package Micro.Targeting

import Micro.Intentions.Intention
import ProxyBwapi.UnitInfo.UnitInfo

trait EvaluateTarget {
  def evaluate(intent:Intention, target:UnitInfo):Double
}
