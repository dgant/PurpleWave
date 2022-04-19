package Planning.Predicates.Milestones

import Planning.Predicates.{MacroFacts, Predicate}

case class GasPumpsAtLeast(minPumps: Int) extends Predicate {
  override def apply: Boolean = MacroFacts.gasPumps >= minPumps
}
