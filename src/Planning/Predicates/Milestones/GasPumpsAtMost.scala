package Planning.Predicates.Milestones

import Planning.MacroFacts
import Planning.Predicates.Predicate

case class GasPumpsAtMost(maxPumps: Int) extends Predicate {
  override def apply: Boolean = MacroFacts.gasPumps <= maxPumps
}
