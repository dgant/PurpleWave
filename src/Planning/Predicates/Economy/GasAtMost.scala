package Planning.Predicates.Economy

import Lifecycle.With
import Planning.Predicate

class GasAtMost(value: Int) extends Predicate {
  
  override def apply: Boolean = With.self.gas <= value
  
}
