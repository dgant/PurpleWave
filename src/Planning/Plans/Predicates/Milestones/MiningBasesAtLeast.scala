package Planning.Plans.Predicates.Milestones

import Planning.Plan

class MiningBasesAtLeast(requiredBases: Int) extends Plan {
  
  override def isComplete: Boolean = AllMiningBases().size >= requiredBases
  
}
