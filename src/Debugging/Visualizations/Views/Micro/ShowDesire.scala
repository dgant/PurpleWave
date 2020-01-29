package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import bwapi.Color

object ShowDesire extends View {
  
  override def renderMap() {
    if (With.blackboard.mcrs()) {
      With.units.playerOwned.foreach(renderMCRS)
    } else {
      With.units.ours.foreach(renderUnitState)
    }
  }

  def renderMCRS(unit: UnitInfo): Unit = {
    if (!unit.possiblyStillThere) return
    if (!With.viewport.contains(unit.tileIncludingCenter)) return
    DrawMap.label(
      "%d: %ds %dd %dt".format(
        PurpleMath.nanToN(10 * unit.mcrs.sim().simValue, 10).toInt,
        unit.mcrs.survivability().toInt / 50,
        unit.mcrs.dpsGround().toInt * 10,
        unit.mcrs.strengthGround().toInt / 100),
      unit.pixelCenter,
      true,
      if (unit.mcrs.shouldFight) Colors.DarkGreen else Colors.DarkRed)
  }
  
  def renderUnitState(unit: FriendlyUnitInfo) {
    val agent = unit.agent
    if ( ! With.viewport.contains(unit.pixelCenter)) return
    if ( ! unit.unitClass.orderable) return
    if (unit.battle.isEmpty) return
    
    var x = unit.pixelCenter.x
    var y = unit.pixelCenter.y + 28
    var width = 18
    
    width = 27
    x = unit.pixelCenter.x
    y = unit.pixelCenter.y + 28 + With.visualization.lineHeightSmall + 2
    drawNumber(unit.matchups.vpfDealingInRange, x - width,  y, width, With.self.colorMedium)
    drawNumber(unit.matchups.vpfReceiving,      x,          y, width, With.enemy.colorMedium)
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
