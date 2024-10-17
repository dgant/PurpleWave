package Planning.Plans.Macro

import Planning.Plan
import Planning.Plans.Gameplans.All.MacroActions
import ProxyBwapi.Buildable

class Cancel(buildables: Buildable*) extends Plan with MacroActions {
  override def onUpdate(): Unit = {
    cancel(buildables: _*)
  }
}
