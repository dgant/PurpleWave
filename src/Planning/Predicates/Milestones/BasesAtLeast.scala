package Planning.Predicates.Milestones

import Planning.Predicates.{MacroFacts, Predicate}

case class BasesAtLeast(requiredBases: Int) extends Predicate {
  override def apply: Boolean = MacroFacts.bases >= requiredBases
}
