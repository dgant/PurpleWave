package Debugging.Visualizations.Views.Fun

import Debugging.Visualizations.Rendering.{DrawMap, DrawScreen}
import Debugging.Visualizations.Views.DebugView
import Lifecycle.With

object ShowKills extends DebugView {

  override def renderScreen(): Unit = {
    val kills = With.units.ours
      .filter(_.kills > 0)
      .toVector
      .sortBy(- _.kills)
      .zipWithIndex

    if (kills.nonEmpty) {
      val text = Vector("High score (Kills)") ++ kills.map(p => "#" + (p._2 + 1) + " " + p._1.unitClass.toString + ": " + p._1.kills)
      DrawScreen.column(5, 5 * With.visualization.lineHeightSmall, text)
    }
  }

  override def renderMap(): Unit = {
    With.units.ours.foreach(unit => {
      val t1 = 5
      val t2 = 25
      val kills = unit.kills
      val x2 = kills >= t1
      val x3 = kills >= t2
      val skulls = kills / (if (x3) t2 else if (x2) t1 else 1)
      val scale = if (x3) 3 else if (x2) 2 else 1
      val w = 10 * scale + 2
      val dx = (skulls - 1) * w / 2
      (0 until skulls).foreach(kill => DrawMap.drawSkull(unit.pixel.add(kill * w - dx, -8 * (scale + 1)), f = scale))
    })
  }
}
