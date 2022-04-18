package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Rendering.{DrawMap, DrawScreen}
import Debugging.Visualizations.Views.DebugView
import Lifecycle.With
import Mathematics.Maff
import ProxyBwapi.UnitInfo.UnitInfo

object ShowDoom extends DebugView {
  override def renderMap(): Unit = {
    With.units.all.foreach(renderUnit)
  }

  @inline private final def renderUnit(unit: UnitInfo): Unit = {
    if (unit.doomed) {
      val frames = With.framesUntil(unit.doomFrameAbsolute)
      if (frames < 96) {
        DrawMap.drawSkull(unit.pixel, f = 3 - Maff.clamp(frames / 24, 0, 2))
      }
    }
  }

  override def renderScreen(): Unit = {
    val u = With.units.all.maxBy(_.damageQueue.size)
    if (u.damageQueue.nonEmpty) {
      DrawScreen.column(5, 5 * With.visualization.lineHeightSmall,
        Seq(
          u.toString,
          if (u.doomed) f"DOOMED in ${With.framesUntil(u.doomFrameAbsolute)}" else "")
          ++ u.damageQueue.map(_.toString))
    }
  }
}
