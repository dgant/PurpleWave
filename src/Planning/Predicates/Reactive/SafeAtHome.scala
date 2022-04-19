package Planning.Predicates.Reactive

import Planning.Predicates.{MacroFacts, Predicate}

case class SafeAtHome() extends Predicate {
  override def apply: Boolean = MacroFacts.safeAtHome
}