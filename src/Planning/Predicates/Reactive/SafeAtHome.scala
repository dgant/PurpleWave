package Planning.Predicates.Reactive

import Planning.Predicate
import Planning.Predicates.MacroFacts

case class SafeAtHome() extends Predicate {
  override def apply: Boolean = MacroFacts.safeAtHome
}