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
      .filter(_.agent.microDecisions.nonEmpty)
      .filter(unit => With.framesSince(unit.agent.microDecisionsUpdateFrame) < 24)
    units = units.take(1)
    units.foreach(renderMapUnit)
  }
  
  def renderMapUnit(unit: FriendlyUnitInfo) {
    unit.agent.microDecisions.foreach(_.renderMap())
  }
}
