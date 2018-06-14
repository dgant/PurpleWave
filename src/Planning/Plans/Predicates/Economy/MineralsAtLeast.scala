package Planning.Plans.Predicates.Economy

import Lifecycle.With
import Planning.Predicate

class MineralsAtLeast(value: Int) extends Predicate {
  
  override def isComplete: Boolean = With.self.minerals >= value
  
}
