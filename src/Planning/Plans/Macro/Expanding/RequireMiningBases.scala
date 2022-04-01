package Planning.Plans.Macro.Expanding

import Debugging.English
import Planning.Predicates.MacroFacts

class RequireMiningBases(basesInitial: Int = 1) extends RequireBases(basesInitial) {

  override protected def basesNow: Int = MacroFacts.miningBases

  override def toString: String = f"Require $basesInitial ${English.pluralize("mining base", basesInitial)}"
}
