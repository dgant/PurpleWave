package Planning.Predicates.Milestones

import Planning.Predicates.{MacroFacts, Predicate}

case class MiningBasesAtLeast(requiredBases: Int) extends Predicate {
  override def apply: Boolean = MacroFacts.miningBases >= requiredBases
}
