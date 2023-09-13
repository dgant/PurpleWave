package Planning.Predicates.Strategy

import Planning.MacroFacts
import Planning.Predicates.Predicate
import Strategery.Strategies.Strategy

case class Employing(strategies: Strategy*) extends Predicate {
  override def apply: Boolean = MacroFacts.employing(strategies: _*)
}
