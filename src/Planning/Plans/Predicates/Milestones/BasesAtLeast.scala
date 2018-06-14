package Planning.Plans.Predicates.Milestones

import Lifecycle.With
import Planning.Plan

class BasesAtLeast(requiredBases: Int) extends Plan {
  
  override def isComplete: Boolean = With.geography.ourBases.size >= requiredBases
  
}
