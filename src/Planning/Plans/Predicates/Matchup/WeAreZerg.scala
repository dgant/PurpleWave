package Planning.Plans.Predicates.Matchup

import Lifecycle.With
import Planning.Plan

class WeAreZerg extends Plan {
  
  override def isComplete: Boolean = With.self.isZerg
  
}
