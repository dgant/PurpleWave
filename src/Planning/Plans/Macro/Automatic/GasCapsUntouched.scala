package Planning.Plans.Macro.Automatic

import Planning.Predicates.{MacroFacts, Predicate}

class GasCapsUntouched extends Predicate {
  override def apply: Boolean = MacroFacts.gasCapsUntouched
}
