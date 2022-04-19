package Planning.Predicates.Milestones

import Planning.Predicates.{MacroFacts, Predicate}

case class MiningBasesAtMost(requiredBases: Int) extends Predicate {
  override def apply: Boolean = MacroFacts.miningBases <= requiredBases
}
