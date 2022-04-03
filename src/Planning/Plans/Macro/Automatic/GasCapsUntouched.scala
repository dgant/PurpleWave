package Planning.Plans.Macro.Automatic

import Planning.Predicate
import Planning.Predicates.MacroFacts

class GasCapsUntouched extends Predicate {
  override def apply: Boolean = MacroFacts.gasCapsUntouched
}
