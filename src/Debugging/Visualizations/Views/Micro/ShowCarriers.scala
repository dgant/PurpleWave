package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Lifecycle.With
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.Orders

object ShowCarriers extends View {

  override def renderMap(): Unit = {
    With.units.ours.foreach(carrier => {
      if (carrier.complete && carrier.is(Protoss.Carrier)) {
        carrier.orderTarget.foreach(target => DrawMap.line(carrier.pixelCenter, target.pixelCenter, Colors.MediumRed))
        DrawMap.box(carrier.pixelCenter.subtract(32, 16), carrier.pixelCenter.add(32, 16), Colors.DarkViolet, true)
        With.game.setTextSize(bwapi.Text.Size.Enum.Large)
        carrier.agent.lastAction.foreach(action => DrawMap.label(action.name, carrier.pixelCenter.subtract(16, 16)))
        DrawMap.label(
          + carrier.interceptors.count(_.order == Orders.InterceptorAttack)
          + "/"
          + carrier.interceptors.count(_.complete),
          carrier.pixelCenter.subtract(16, 0))
        With.game.setTextSize(bwapi.Text.Size.Enum.Small)
        if (carrier.interceptors.exists(_.complete)) {
          DrawMap.circle(carrier.pixelCenter, 32*8, Colors.BrightGray)
          DrawMap.circle(carrier.pixelCenter, 32*10, Colors.DarkGray)
        }
      }
    })
  }

  override def renderScreen(): Unit = {

  }
}
