package Debugging.Visualizations.Views.Economy

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Views.DebugView
import Lifecycle.With

object ShowGas extends DebugView {

  override def renderScreen(): Unit = {
    val workersMin  = With.blackboard.gasWorkerFloor()
    val workersNow  = With.units.countOurs(_.friendly.exists(_.agent.toGather.exists(_.unitClass.isGas)))
    val workersMax  = With.blackboard.gasWorkerCeiling()
    val gasMin      = With.blackboard.gasLimitFloor()
    val gasNow      = With.self.gas
    val gasMax      = With.blackboard.gasLimitCeiling()

    val bars = Seq(
      (workersMin, Colors.DarkBlue,     "WorkersMin", 32, false),
      (workersNow, Colors.MediumBlue,   "WorkersNow", 32, workersNow < workersMin || workersNow > workersMax),
      (workersMax, Colors.NeonBlue,     "WorkersMax", 32, false),
      (gasMin,     Colors.DarkGreen,    "GasMin",     1,  false),
      (gasNow,     Colors.MediumGreen,  "GasNow",     1,  gasNow < gasMin || gasNow > gasMax),
      (gasMax,     Colors.NeonGreen,    "GasMax",     1,  false))

    val barHeight = 16
    val barMargin = 4

    bars
      .zipWithIndex
      .foreach(p => {
        val bar     = p._2
        val count   = p._1._1
        val color   = p._1._2
        val label   = p._1._3
        val size    = count * p._1._4
        val isBad   = p._1._5
        val x0      = 0
        val x1      = size
        val y0      = With.visualization.lineHeightSmall * 4 + bar * (barMargin + barHeight)
        val y1      = y0 + barHeight
        val yText   = y0 + barHeight / 2 - With.visualization.lineHeightSmall / 2
        With.game.drawBoxScreen(x0, y0, x1, y1, color, true)
        if (isBad) {
          With.game.drawBoxScreen(x0, y0, x1, y1, Colors.BrightRed, false)
        }
        With.game.drawTextScreen(x0, yText, count.toString)
        With.game.drawTextScreen(x0 + 48, yText, label)

    })
  }
}