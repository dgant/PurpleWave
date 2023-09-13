package Planning.Predicates.Reactive

import Planning.MacroFacts
import Planning.Predicates.Predicate

case class SafeAtHome() extends Predicate {
  override def apply: Boolean = MacroFacts.safeDefending
}