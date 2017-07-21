package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Views.View
import Lifecycle.With
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object ShowMicroDecisions extends View {
  
  override def renderMap() {
    var units = With.units.ours.filter(_.selected)
    if (units.isEmpty) units = With.units.ours.filter(_.battle.exists(_.happening))
    units = units
      .filter(unit => With.viewport.contains(unit.pixelCenter))
      .filter(_.action.microDecisions.nonEmpty)
      .filter(unit => With.framesSince(unit.action.microDecisionsUpdateFrame) < 24)
    units = units.take(1)
    units.foreach(renderMapUnit)
  }
  
  def renderMapUnit(unit: FriendlyUnitInfo) {
    unit.action.microDecisions.foreach(_.renderMap())
  }
}
