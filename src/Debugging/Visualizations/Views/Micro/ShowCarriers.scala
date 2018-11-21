package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Lifecycle.With
import ProxyBwapi.Races.Protoss

object ShowCarriers extends View {

  override def renderMap(): Unit = {
    With.units.ours.foreach(carrier => {
      if (carrier.complete && carrier.is(Protoss.Carrier)) {
        carrier.target.map(target => DrawMap.line(carrier.pixelCenter, target.pixelCenter, Colors.DarkRed))
        With.game.setTextSize(bwapi.Text.Size.Enum.Large)
        carrier.agent.lastAction.map(action => DrawMap.label(action.name, carrier.pixelCenter.subtract(0, 12)))
        DrawMap.label(
          + carrier.interceptors.count(_.visible)
          + "/"
          + carrier.interceptors.count(_.moving)
          + "/"
          + carrier.interceptors.size,
          carrier.pixelCenter.add(0, 12))
        With.game.setTextSize(bwapi.Text.Size.Enum.Small)
        carrier.interceptors.foreach(interceptor => {
          interceptor.target.map(target => DrawMap.line(interceptor.pixelCenter, target.pixelCenter, Colors.DarkRed))
          DrawMap.circle(interceptor.pixelCenter, interceptor.unitClass.width/2, if (interceptor.visible) Colors.BrightGreen else Colors.DarkGray)
        })
      }
    })
  }

  override def renderScreen(): Unit = {

  }
}
