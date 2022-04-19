package Planning.Predicates.Milestones

import Planning.Predicates.{MacroFacts, Predicate}

case class GasPumpsAtMost(maxPumps: Int) extends Predicate {
  override def apply: Boolean = MacroFacts.gasPumps <= maxPumps
}
