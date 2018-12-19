package Planning.Plans.Macro.Build

import Lifecycle.With
import Planning.Plan
import Planning.UnitMatchers.UnitMatcher

class CancelAll(matcher: UnitMatcher) extends Plan {

  override def onUpdate(): Unit = {
    // TODO: Crude
    With.units.ours.view.filter(u => ! u.complete && u.is(matcher)).foreach(With.commander.cancel)
  }
}
