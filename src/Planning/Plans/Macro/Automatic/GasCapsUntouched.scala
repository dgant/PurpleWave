package Planning.Plans.Macro.Automatic

import Planning.MacroFacts
import Planning.Predicates.Predicate

class GasCapsUntouched extends Predicate {
  override def apply: Boolean = MacroFacts.gasCapsUntouched
}
