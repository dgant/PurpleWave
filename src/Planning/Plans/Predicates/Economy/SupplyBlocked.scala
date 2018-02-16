package Planning.Plans.Predicates.Economy

import Lifecycle.With
import Planning.Plan

class SupplyBlocked extends Plan {
  
  override def isComplete: Boolean = With.self.supplyUsed >= With.self.supplyTotal
  
}
