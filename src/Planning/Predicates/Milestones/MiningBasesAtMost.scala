package Planning.Predicates.Milestones

import Planning.{Plan, Predicate}

class MiningBasesAtMost(requiredBases: Int) extends Predicate {
  
  override def apply: Boolean = AllMiningBases().size <= requiredBases
  
}
