package Planning.Predicates

import Planning.{Plan, Predicate}
import Strategery.Strategies.Strategy

class Employing(strategies: Strategy*) extends Predicate {
  
  override def toString: String = "Employing " + strategies.mkString("/")
  
  override def isComplete: Boolean = strategies.exists(_.active)
}
