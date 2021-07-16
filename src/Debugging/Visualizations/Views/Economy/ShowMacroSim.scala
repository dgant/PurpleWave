package Debugging.Visualizations.Views.Economy

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Lifecycle.With
import Mathematics.Points.Pixel
import bwapi.Color

object ShowMacroSim extends View {
  val y0 = 240
  val pixelsPerFrame: Double = 2.0 / 24.0
  val pixelsPerResource: Double = 1.0 / 8.0
  val pixelsPerSupply: Double = 1.0
  val widthThreshold: Double = 10

  val colorMinerals: Color = Colors.NeonTeal
  val colorGas: Color = Colors.NeonGreen
  val colorSupplyComplete: Color = Colors.White
  val colorSupplyIncomplete: Color = Colors.MediumGray
  val colorWorkersComplete: Color = Colors.BrightRed
  val colorWorkersIncomplete: Color = Colors.DarkRed
  val colorUnitsComplete: Color = Colors.BrightBlue
  val colorUnitsIncomplete: Color = Colors.DarkBlue

  override def renderMap(): Unit = {
    val sim = With.macroSim
    var x0 = 5
    var i = 0
    var frameBefore = -24
    while (i < sim.states.size && sim.states(i).exists) {
      val state                 = sim.states(i)
      val dFrame                = state.framesAhead
      val width                 = Math.ceil((dFrame - frameBefore) * pixelsPerFrame).toInt
      val x1                    = x0 + width
      val ySupplyIncomplete     = y0 - pixelsPerSupply * state.units.view.map(p => p._1.supplyProvided * p._2).sum
      val ySupplyComplete       = y0 - pixelsPerSupply * state.supplyAvailable
      val ySupplyUsedIncomplete = y0 - pixelsPerSupply * state.units.view.map(p => p._1.supplyRequired * p._2).sum
      val ySupplyUsedComplete   = y0 - pixelsPerSupply * state.unitsAvailable.view.map(p => p._1.supplyRequired * p._2).sum
      val yWorkersIncomplete    = y0 - pixelsPerSupply * state.units.view.filter(_._1.isWorker).map(_._2).sum
      val yWorkersComplete      = y0 - pixelsPerSupply * state.unitsAvailable.view.filter(_._1.isWorker).map(_._2).sum
      val yMinerals             = y0 + pixelsPerResource * state.minerals
      val yGas                  = y0 + pixelsPerResource * state.gas
      def draw(y: Double, color: Color): Unit = {
        val y1 = Math.round(y).toInt
        DrawMap.box(Pixel(x0, Math.min(y0, y1)), Pixel(x1, Math.max(y0, y1)), color, solid = true)
      }
      if (yMinerals > yGas) {
        draw(yMinerals, colorMinerals)
        draw(yGas, colorGas)
      } else {
        draw(yGas, colorGas)
        draw(yMinerals, colorMinerals)
      }
      draw(ySupplyIncomplete,     colorSupplyIncomplete)
      draw(ySupplyComplete,       colorSupplyComplete)
      draw(ySupplyUsedIncomplete, colorUnitsIncomplete)
      draw(ySupplyUsedComplete,   colorUnitsComplete)
      draw(yWorkersIncomplete,    colorWorkersIncomplete)
      draw(yWorkersComplete,      colorWorkersComplete)
      x0 = x1
      i += 1
    }
  }
}
