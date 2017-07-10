package Planning.Plans.Information

import Lifecycle.With
import Planning.Plan
import Strategery.Strategies.Strategy

class Employing(strategy: Strategy) extends Plan {
  
  override def toString: String = "Employing " + strategy
  
  override def isComplete: Boolean = {
    With.strategy.selected.contains(strategy)
  }
}
