package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Rendering.{DrawMap, DrawScreen}
import Debugging.Visualizations.Views.View
import Lifecycle.With
import Mathematics.Maff
import ProxyBwapi.UnitInfo.UnitInfo

object ShowDoom extends View {
  override def renderMap(): Unit = {
    With.units.all.foreach(renderUnit)
  }

  @inline private final def renderUnit(unit: UnitInfo): Unit = {
    if (unit.doomed) {
      val frames = With.framesUntil(unit.doomFrame)
      if (frames < 96) {
        DrawMap.drawSkull(unit.pixel, f = 3 - Maff.clamp(frames / 24, 0, 2))
      }
    }
  }

  override def renderScreen(): Unit = {
    With.units.all.find(_.selected).foreach(u =>
      DrawScreen.column(5, 3 * With.visualization.lineHeightSmall,
        Seq(
          u.toString,
          if (u.doomed) "DOOMED" else "")
        ++ u.damageQueue.map(_.toString)))
  }
}
