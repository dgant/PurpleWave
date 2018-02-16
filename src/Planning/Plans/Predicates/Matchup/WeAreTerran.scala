package Planning.Plans.Predicates.Matchup

import Lifecycle.With
import Planning.Plan

class WeAreTerran extends Plan {
  
  override def isComplete: Boolean = With.self.isTerran
  
}
