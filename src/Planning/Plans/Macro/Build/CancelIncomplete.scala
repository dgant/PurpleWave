package Planning.Plans.Macro.Build

import Lifecycle.With
import Micro.Agency.Commander
import Planning.Plan
import Planning.UnitMatchers._

class CancelIncomplete(matchers: UnitMatcher*) extends Plan {
  override def onUpdate(): Unit = {
    val incompletes = With.units.ours.view.filter(u => ! u.complete && u.isAny(matchers: _*))

    // Hack
    incompletes.foreach(Commander.cancel)
    incompletes.filterNot(_.unitClass.isBuilding).flatMap(_.buildUnit).flatMap(_.friendly).foreach(Commander.cancel)
  }
}