package Planning.Predicates.Reactive

import Planning.MacroFacts
import Planning.Predicates.Predicate

case class SafeToMoveOut() extends Predicate {
  override def apply: Boolean = MacroFacts.safePushing
}