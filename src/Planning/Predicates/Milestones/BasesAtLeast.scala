package Planning.Predicates.Milestones

import Planning.MacroFacts
import Planning.Predicates.Predicate

case class BasesAtLeast(requiredBases: Int) extends Predicate {
  override def apply: Boolean = MacroFacts.bases >= requiredBases
}
