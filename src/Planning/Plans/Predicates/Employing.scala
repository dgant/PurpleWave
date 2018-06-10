package Planning.Plans.Predicates

import Planning.Plan
import Strategery.Strategies.Strategy

class Employing(strategies: Strategy*) extends Plan {
  
  override def toString: String = "Employing " + strategies.mkString("/")
  
  override def isComplete: Boolean = strategies.exists(_.active)
}
