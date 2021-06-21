package Planning.Predicates.Milestones

import Planning.Predicate
import Planning.Predicates.MacroFacts

case class BasesAtLeast(requiredBases: Int) extends Predicate {
  override def apply: Boolean = MacroFacts.bases >= requiredBases
}
