package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.DebugView
import Lifecycle.With
import Micro.Actions.Zerg.BeMutalisk.MutaliskRecovering
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object ShowMutalisks extends DebugView {
  override def renderMap(): Unit = {
    With.units.all.view
      .flatMap(_.friendly)
      .filter(Zerg.Mutalisk)
      .foreach(renderUnit)
  }

  @inline private final def renderUnit(unit: FriendlyUnitInfo): Unit = {
    if (unit.agent.contains(MutaliskRecovering)) {
      DrawMap.box(unit.topLeft, unit.bottomRightExclusive, Colors.NeonYellow)

      unit.agent.path.foreach(_.renderMap(Colors.NeonYellow, Some(unit.pixel)))
    }
  }
}
