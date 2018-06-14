package Planning.Plans.Predicates.Milestones

import Planning.{Plan, Predicate}

class MiningBasesAtMost(requiredBases: Int) extends Predicate {
  
  override def isComplete: Boolean = AllMiningBases().size <= requiredBases
  
}
