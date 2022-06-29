package Planning.Plans.Placement

import Placement.Access.PlaceLabels.PlaceLabel
import Planning.Plan
import Planning.Plans.GamePlans.All.MacroActions

class BuildCannonsAtNatural(count: Int, labels: PlaceLabel*) extends Plan with MacroActions {
  override def onUpdate(): Unit = {
    buildCannonsAtNatural(count, labels: _*)
  }
}
