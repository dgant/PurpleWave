package Planning.Predicates.Milestones

import Lifecycle.With
import Planning.Predicate

class GasPumpsAtMost(maxPumps: Int) extends Predicate {
  
  override def apply: Boolean =
    With.geography.ourBases
      .map(_.gas.count(_.gasLeft > 300))
      .sum <= maxPumps
}
