package Planning.Plans.Macro.Milestones

import Planning.Plan

class MiningBasesAtLeast(requiredBases: Int) extends Plan {
  
  description.set("We have " + requiredBases + "+ mining bases.")
  
  override def isComplete: Boolean = AllMiningBases().size >= requiredBases
}
