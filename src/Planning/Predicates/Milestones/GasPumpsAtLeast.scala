package Planning.Predicates.Milestones

import Planning.MacroFacts
import Planning.Predicates.Predicate

case class GasPumpsAtLeast(minPumps: Int) extends Predicate {
  override def apply: Boolean = MacroFacts.gasPumps >= minPumps
}
