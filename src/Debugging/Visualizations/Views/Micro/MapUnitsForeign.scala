package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Lifecycle.With
import ProxyBwapi.UnitInfo.ForeignUnitInfo

object MapUnitsForeign {
  
  def render() {
    With.units.enemy.foreach(drawTrackedUnit)
    With.units.neutral.foreach(drawTrackedUnit)
  }
  
  private def drawTrackedUnit(unit:ForeignUnitInfo) {
    if ( ! With.viewport.contains(unit.pixelCenter)) return
    
    if ( ! unit.visible) {
      val color =
        if (unit.likelyStillThere)
          unit.player.colorDark
        else if(unit.possiblyStillThere)
          unit.player.colorMidnight
        else
          Colors.MidnightGray
        
      DrawMap.circle(
        unit.pixelCenter,
        unit.unitClass.width / 2,
        unit.player.colorDark)
      DrawMap.label(
        unit.unitClass.toString,
        unit.pixelCenter,
        drawBackground = true,
        unit.player.colorDark)
    }
  }
}
