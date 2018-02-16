package Planning.Plans.Predicates.Matchup

import Lifecycle.With
import Planning.Plan

class WeAreProtoss extends Plan {
  
  override def isComplete: Boolean = With.self.isProtoss
  
}
