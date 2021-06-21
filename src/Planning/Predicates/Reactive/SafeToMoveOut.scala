package Planning.Predicates.Reactive

import Planning.Predicate
import Planning.Predicates.MacroFacts

case class SafeToMoveOut() extends Predicate {
  override def apply: Boolean = MacroFacts.safeToMoveOut
}