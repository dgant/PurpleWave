package Planning.Plans.Macro.Milestones

import Lifecycle.With
import Planning.Plan

class HaveGasBases(requiredBases: Int) extends Plan {
  
  description.set("We have " + requiredBases + "+ gas bases.")
  
  override def isComplete: Boolean =
    With.geography.ourBases
      .count(base =>
        base.gas.nonEmpty &&
        base.gasLeft > 300) >= requiredBases
  
}
