package Planning.Plans.Macro.Protoss

import Planning.Plan
import Planning.Plans.GamePlans.MacroActions

class MeldDarkArchons extends Plan with MacroActions {
  override def onUpdate() {
    makeDarkArchons()
  }
}
