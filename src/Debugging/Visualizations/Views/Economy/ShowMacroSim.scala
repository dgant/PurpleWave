package Debugging.Visualizations.Views.Economy

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawScreen
import Debugging.Visualizations.Views.DebugView
import Lifecycle.With
import Utilities.Time.Seconds
import bwapi.Color

object ShowMacroSim extends DebugView {
  val y0 = 300
  val pixelsPerFrame          : Double = 638.0 / Seconds(90)()
  val pixelsPerResource       : Double = y0 / 1000.0
  val pixelsPerSupply         : Double = y0 / 400.0
  val colorMinerals           : Color = Colors.NeonTeal
  val colorGas                : Color = Colors.BrightGreen
  val colorSupplyUsed         : Color = Colors.BrightRed
  val colorSupplyAvailable    : Color = Colors.MidnightRed

  private def x0i(i: Int): Int = 1 + (With.macroSim.steps(i).event.dFrames * pixelsPerFrame).toInt

  override def renderScreen(): Unit = {
    val steps = With.macroSim.steps
    if (steps.isEmpty) return
    var pass = 0
    while (pass < 2) {
      var i = 0
      while (i < steps.size) {
        val x0 = x0i(i)
        val x1 = if (i == steps.size - 1) 639 else 1 + Math.ceil(steps(i + 1).event.dFrames * pixelsPerFrame).toInt
        val state = steps(i).state
        def box(color: Color, y: Double, isSolid: Boolean): Unit = With.game.drawBoxScreen(x0, (y0 - y).toInt, x1, y0, color, isSolid)
        if (pass == 0) {
          val ySupplyAvailable = pixelsPerSupply * state.supplyAvailable
          val ySupplyUsed = pixelsPerSupply * state.supplyUsed
          box(colorSupplyAvailable, ySupplyAvailable, isSolid = true)
          box(colorSupplyUsed, ySupplyUsed, isSolid = true)
          box(colorSupplyAvailable, ySupplyAvailable, isSolid = false)
        } else if (pass == 1) {
          if (i < steps.size - 1) {
            val y0Minerals = (y0 - pixelsPerResource * steps(i).state.minerals).toInt
            val y1Minerals = (y0 - pixelsPerResource * steps(i + 1).state.minerals).toInt
            val y0Gas = (y0 - pixelsPerResource * steps(i).state.gas).toInt
            val y1Gas = (y0 - pixelsPerResource * steps(i + 1).state.gas).toInt
            With.game.drawLineScreen(x0, y0Minerals, x1, y1Minerals, colorMinerals)
            With.game.drawLineScreen(x0, y0Gas, x1, y1Gas, colorGas)
          }
        }
        i += 1
      }
      pass += 1
    }
    var j = 1
    while (j < steps.size) {
      val x0 = x0i(j)
      DrawScreen.text(x0, y0 - With.visualization.lineHeightSmall * j - 2, steps(j).toString)
      j += 1
    }
  }

}
