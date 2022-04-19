package Planning.Plans.Scouting

import Planning.Predicates.{MacroFacts, Predicate}

class ScoutCleared extends Predicate {
  override def apply: Boolean = MacroFacts.scoutCleared
}
