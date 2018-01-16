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
    With.agents.all.foreach(renderUnitState)
  }
  
  def renderUnitState(agent: Agent) {
    if ( ! With.viewport.contains(agent.unit.pixelCenter)) return
    if ( ! agent.unit.unitClass.orderable) return
    if (agent.unit.battle.isEmpty) return
    
    
    var x = agent.unit.pixelCenter.x
    var y = agent.unit.pixelCenter.y + 28
    var width = 18
    drawDesire(agent.netEngagementValue, x - width / 2, y, width)
    
    width = 27
    x = agent.unit.pixelCenter.x
    y = agent.unit.pixelCenter.y + 28 + With.visualization.lineHeightSmall + 2
    drawNumber(agent.unit.matchups.vpfDealingDiffused,    x - width,  y, width, With.self.colorMedium)
    drawNumber(agent.unit.matchups.vpfReceivingDiffused,  x,          y, width, With.enemy.colorMedium)
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
