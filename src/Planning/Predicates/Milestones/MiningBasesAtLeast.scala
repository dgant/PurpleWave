package Planning.Predicates.Milestones

import Planning.Predicate

class MiningBasesAtLeast(requiredBases: Int) extends Predicate {
  
  override def isComplete: Boolean = AllMiningBases().size >= requiredBases
  
}
