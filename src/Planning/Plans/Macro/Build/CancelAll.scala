package Planning.Plans.Macro.Build

import Lifecycle.With
import Planning.Plan
import Planning.UnitMatchers.UnitMatcher

class CancelAll(matchers: UnitMatcher*) extends Plan {

  override def onUpdate(): Unit = {
    // TODO: Crude
    With.units.ours.view.filter(u => ! u.complete && u.isAny(matchers: _*)).foreach(With.commander.cancel)
  }
}
