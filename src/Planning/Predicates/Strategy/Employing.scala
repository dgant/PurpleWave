package Planning.Predicates.Strategy

import Planning.Predicate
import Strategery.Strategies.Strategy

class Employing(strategies: Strategy*) extends Predicate {
  
  override def toString: String = "Employing " + strategies.mkString("/")
  
  override def apply: Boolean = strategies.exists(_.registerActive())
}
