package Planning.Predicates.Economy

import Lifecycle.With
import Planning.Predicate

class MineralsAtLeast(value: Int) extends Predicate {
  
  override def apply: Boolean = With.self.minerals >= value
  
}
