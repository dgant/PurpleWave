package Planning.Plans.Predicates.Economy

import Lifecycle.With
import Planning.Plan

class GasAtLeast(value: Int) extends Plan {
  
  override def isComplete: Boolean = With.self.gas >= value
  
}
