package Debugging.Visualization.Views

import Debugging.Visualization.Rendering.DrawMap
import Startup.With

object VisualizeTextOnly {
  
  def render() {
    
    With.units.all.filter(_.visible).filter(u => u.complete || u.unitClass.isBuilding).foreach(unit => {
      val color = DrawMap.playerColor(unit.player)
      val text = List(
        unit.player.getName,
        unit.unitClass.toString,
        if (unit.player == With.neutral) "" else unit.totalHealth.toString + "/" + unit.unitClass.maxTotalHealth,
        if (unit.complete) "" else "In progress"
      )
      DrawMap.labelBox(text, unit.pixelCenter, false)
    })
    
  }
  
}
