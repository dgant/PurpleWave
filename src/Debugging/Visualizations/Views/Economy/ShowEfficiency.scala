package Debugging.Visualizations.Views.Economy

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawScreen
import Debugging.Visualizations.Views.DebugView
import Lifecycle.With
import bwapi.Color

object ShowEfficiency extends DebugView {

  case class Efficiency(name: String, index: Int, denominator: Int, numerator: Int, streak: Int) {
    val xEnd: Int = 640
    val w   : Int = denominator / 24 / 15
    val y   : Int = 315 - 15 * index
    val x0  : Int = xEnd - w
    val x1  : Int = xEnd - Math.ceil(w.toFloat * numerator / denominator).toInt
    val x2  : Int = xEnd - Math.ceil(w.toFloat * streak    / denominator).toInt
    val x3  : Int = x0 - 5 * name.length - 3
    val xc  : Int = x3 - 7
    val yc  : Int = y + 7
    val yEnd: Int = y + 13
    def renderScreen(): Unit = {
      With.game.drawBoxScreen(x0, y, xEnd, yEnd, Color.Black,         true)
      With.game.drawBoxScreen(x1, y, xEnd, yEnd, Colors.MediumOrange, true)
      With.game.drawBoxScreen(x2, y, xEnd, yEnd, Colors.NeonRed,      true)
      DrawScreen.text(x3, y + 1, name)
      if (streak > 0) {
        With.game.drawCircleScreen(xc, yc, 5, Colors.NeonRed, true)
      }
    }
  }

  override def renderScreen(): Unit = {
    val producers = With.units.ours
      .filter(u => u.unitClass.trainsUnits && u.complete && u.unitClass.isBuilding)
      .toVector
      .sortBy(_.completionFrame)
    val labels = producers.indices.map(i => {
      val p = producers(i)
      Efficiency(
        p.unitClass.toString,
        i + 1,
        With.framesSince(p.completionFrame),
        p.framesIdle,
        p.framesIdleConsecutive)
    }) :+
      Efficiency(
        "Supply block",
        0,
        With.frame,
        With.efficiency.framesSupplyBlocked,
        With.efficiency.framesSupplyBlockedConsecutively)

    labels.foreach(_.renderScreen())
  }
}
