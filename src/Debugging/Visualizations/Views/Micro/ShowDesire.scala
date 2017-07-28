package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Agency.Agent
import bwapi.Color

object ShowDesire extends View {
  
  override def renderMap() {
    With.agents.states.foreach(renderUnitState)
  }
  
  def renderUnitState(state: Agent) {
    if ( ! With.viewport.contains(state.unit.pixelCenter)) return
    if ( ! state.unit.unitClass.orderable) return
    
    if (state.desireTotal != 1.0) {
      val x = state.unit.pixelCenter.x
      val y = state.unit.pixelCenter.y + 28
      val width = 18
      drawDesire(state.desireTeam,        x - width * 3 / 2,  y, width)
      drawDesire(state.desireIndividual,  x - width     / 2,  y, width)
      drawDesire(state.desireTotal,       x + width     / 2,  y, width)
    }
    
    if (state.unit.battle.nonEmpty) {
      val width = 27
      val x = state.unit.pixelCenter.x
      val y = state.unit.pixelCenter.y + 28 + With.visualization.lineHeightSmall + 2
      drawNumber(state.unit.matchups.vpfDealingDiffused,    x - width,  y, width, With.self.colorMedium)
      drawNumber(state.unit.matchups.vpfReceivingDiffused,  x,          y, width, With.enemy.colorMedium)
    }
  }
  
  def drawDesire(desire: Double, x: Int, y: Int, width: Int) {
    val color = if (desire == 1.0) Colors.MediumGray else if (desire > 1.0) Colors.DarkGreen else Colors.MediumRed
    drawNumber(desire, x, y, width, color)
  }
  
  def drawNumber(value: Double, x: Int, y: Int, width: Int, color: Color) {
    DrawMap.box(
      Pixel(x, y),
      Pixel(x + width, y + With.visualization.lineHeightSmall + 2),
      solid = true,
      color = color)
    DrawMap.text(Pixel(x + 2, y), "%1.1f".format(value).replace("Infinity", "Inf"))
  }
}
