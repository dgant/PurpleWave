package Planning.Predicates.Reactive

import Planning.Predicates.{MacroFacts, Predicate}

case class SafeToMoveOut() extends Predicate {
  override def apply: Boolean = MacroFacts.safePushing
}