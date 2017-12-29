package Planning.Plans.Macro.Milestones

import Lifecycle.With
import Planning.Plan

class HaveGasPumps(requiredPumps: Int) extends Plan {
  
  description.set("We have " + requiredPumps + "+ gas pumps.")
  
  override def isComplete: Boolean =
    With.geography.ourBases
      .map(_.gas.count(_.gasLeft > 300))
      .sum >= requiredPumps
  
}
