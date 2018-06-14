package Planning.Plans.Predicates.Milestones

import Lifecycle.With
import Planning.Predicate

class HaveGasPumps(requiredPumps: Int) extends Predicate {
  
  override def isComplete: Boolean =
    With.geography.ourBases
      .map(_.gas.count(_.gasLeft > 300))
      .sum >= requiredPumps
}
