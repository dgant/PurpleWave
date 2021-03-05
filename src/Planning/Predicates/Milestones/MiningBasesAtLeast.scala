package Planning.Predicates.Milestones

import Planning.Predicate

class MiningBasesAtLeast(requiredBases: Int) extends Predicate {
  
  override def apply: Boolean = AllMiningBases().size >= requiredBases
  
}
