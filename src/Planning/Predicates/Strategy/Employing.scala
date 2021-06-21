package Planning.Predicates.Strategy

import Planning.Predicate
import Planning.Predicates.MacroFacts
import Strategery.Strategies.Strategy

case class Employing(strategies: Strategy*) extends Predicate {
  override def apply: Boolean = MacroFacts.employing(strategies: _*)
}
