package Planning.Plans.Predicates.Economy

import Lifecycle.With
import Planning.Plan

class MineralsAtLeast(value: Int) extends Plan {
  
  override def isComplete: Boolean = With.self.minerals >= value
  
}
