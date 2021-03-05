package Planning.Predicates.Economy

import Lifecycle.With
import Planning.Predicate

class SupplyBlocked extends Predicate {
  
  override def apply: Boolean = With.self.supplyUsed >= With.self.supplyTotal
  
}
