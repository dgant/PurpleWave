package Planning.Plans.Macro.Milestones

import Lifecycle.With
import Planning.Plan

class MiningBasesAtLeast(requiredBases: Int) extends Plan {
  
  description.set("Require " + requiredBases + " mining bases.")
  
  override def isComplete: Boolean =
    With.geography.ourBases
      .filter(_.townHall.isDefined)
      .count(base =>
        base.minerals.size >= 6 &&
        base.mineralsLeft / base.minerals.size > 300) >= requiredBases
}
