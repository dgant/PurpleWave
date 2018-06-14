package Planning.Plans.Predicates.Matchup

import Lifecycle.With
import Planning.Predicate

class WeAreProtoss extends Predicate {
  
  override def isComplete: Boolean = With.self.isProtoss
  
}
