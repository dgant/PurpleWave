package Planning.Predicates.Milestones

import Planning.MacroFacts
import Planning.Predicates.Predicate

case class MiningBasesAtLeast(requiredBases: Int) extends Predicate {
  override def apply: Boolean = MacroFacts.miningBases >= requiredBases
}
