package Planning.Predicates.Milestones

import Planning.Predicate
import Planning.Predicates.MacroFacts

case class GasPumpsAtLeast(minPumps: Int) extends Predicate {
  override def apply: Boolean = MacroFacts.gasPumps >= minPumps
}
