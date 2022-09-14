package Planning.Plans.Macro.Expanding

import Debugging.English
import Planning.Plan
import Planning.Plans.GamePlans.All.MacroActions

class RequireMiningBases(bases: Int = 1) extends Plan with MacroActions {

  override def onUpdate(): Unit = {
    requireMiningBases(bases)
  }

  override def toString: String = f"Require $bases ${English.pluralize("mining base", bases)}"
}
