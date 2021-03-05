package Planning.Predicates.Economy

import Lifecycle.With
import Planning.Predicate

class MineralsAtMost(value: Int) extends Predicate {
  
  override def apply: Boolean = With.self.minerals <= value
  
}
