package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Planning.Predicate
import Planning.Predicates.MacroFacts

class GasCapsUntouched extends Predicate {
  override def apply: Boolean = MacroFacts.gasCapsUntouched
}
