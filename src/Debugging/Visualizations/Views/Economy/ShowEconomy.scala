package Debugging.Visualizations.Views.Economy

import Debugging.Visualizations.Rendering.DrawScreen
import Debugging.Visualizations.Views.View
import Lifecycle.With

object ShowEconomy extends View {
  
  override def renderScreen() {
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
    val mineralAccuracyRatio  = With.economy.ourEstimatedTotalMinerals  / With.economy.ourActualTotalMinerals
    val gasAccuracyRatio      = With.economy.ourEstimatedTotalGas       / With.economy.ourActualTotalGas
    val mineralAccuracy       = Math.min(mineralAccuracyRatio, 1.0 / mineralAccuracyRatio)
    val gasAccuracy           = Math.min(gasAccuracyRatio, 1.0 / gasAccuracyRatio)
    val values = Vector(
      With.economy.ourActiveMiners,
      With.economy.ourActiveDrillers,
      (With.economy.ourIncomePerFrameMinerals * 24 * 60).toInt,
      (With.economy.ourIncomePerFrameGas * 24 * 60).toInt,
      With.economy.ourEstimatedTotalMinerals.toInt,
      With.economy.ourActualTotalMinerals,
      With.economy.ourEstimatedTotalGas.toInt,
      With.economy.ourActualTotalGas,
      mineralAccuracy.toString,
      gasAccuracy.toString
    )
    DrawScreen.column(500, 31, labels.mkString("\n"))
    DrawScreen.column(600, 31, values.mkString("\n"))
  }
}
