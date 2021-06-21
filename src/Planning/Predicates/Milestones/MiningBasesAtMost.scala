package Planning.Predicates.Milestones

import Planning.Predicate
import Planning.Predicates.MacroFacts

case class MiningBasesAtMost(requiredBases: Int) extends Predicate {
  override def apply: Boolean = MacroFacts.miningBases <= requiredBases
}
