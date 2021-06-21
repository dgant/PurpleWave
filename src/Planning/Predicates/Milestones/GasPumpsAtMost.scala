package Planning.Predicates.Milestones

import Planning.Predicate
import Planning.Predicates.MacroFacts

case class GasPumpsAtMost(maxPumps: Int) extends Predicate {
  override def apply: Boolean = MacroFacts.gasPumps <= maxPumps
}
