package Planning.Plans.Scouting

import Planning.Predicate
import Planning.Predicates.MacroFacts

class ScoutCleared extends Predicate {
  override def apply: Boolean = MacroFacts.scoutCleared
}
