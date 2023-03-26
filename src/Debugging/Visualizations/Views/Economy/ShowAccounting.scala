package Debugging.Visualizations.Views.Economy

import Debugging.Visualizations.Rendering.DrawScreen
import Debugging.Visualizations.Views.DebugView
import Lifecycle.With
import Mathematics.Maff

object ShowAccounting extends DebugView {
  
  override def renderScreen(): Unit = {
    val labels = Vector(
      "Active miners:",
      "Active drillers:",
      "Minerals per minute:",
      "Gas per minute:",
      "Total minerals (est.):",
      "Total minerals (real):",
      "Total gas (est.):",
      "Total gas (real):",
      "Accuracy (minerals):",
      "Accuracy (gas):"
    )
    val mineralAccuracyRatio  = Maff.nanToOne(With.accounting.ourEstimatedTotalMinerals  / With.accounting.ourActualTotalMinerals)
    val gasAccuracyRatio      = Maff.nanToOne(With.accounting.ourEstimatedTotalGas       / With.accounting.ourActualTotalGas)
    val mineralAccuracy       = Math.min(mineralAccuracyRatio, 1.0 / mineralAccuracyRatio)
    val gasAccuracy           = Math.min(gasAccuracyRatio, 1.0 / gasAccuracyRatio)
    val values = Vector(
      With.accounting.ourActiveMiners,
      With.accounting.ourActiveDrillers,
      (With.accounting.ourIncomePerFrameMinerals * 24 * 60).toInt,
      (With.accounting.ourIncomePerFrameGas * 24 * 60).toInt,
      With.accounting.ourEstimatedTotalMinerals.toInt,
      With.accounting.ourActualTotalMinerals,
      With.accounting.ourEstimatedTotalGas.toInt,
      With.accounting.ourActualTotalGas,
      mineralAccuracy.toString,
      gasAccuracy.toString
    )
    DrawScreen.column(500, 31, labels.mkString("\n"))
    DrawScreen.column(600, 31, values.mkString("\n"))
  }
}
