package Debugging.Visualizations.Views.Fun

import Debugging.Visualizations.Rendering.DrawMap
import Lifecycle.With

object VisualizeTextOnly {
  
  def render() {
    
    With.units.all.filter(_.visible).filter(u => u.complete || u.unitClass.isBuilding).foreach(unit => {
      val color = unit.player.colorDark
      val text = Vector(
        unit.player.name,
        unit.unitClass.toString,
        if (unit.player == With.neutral) "" else unit.totalHealth.toString + "/" + unit.unitClass.maxTotalHealth,
        if (unit.complete) "" else "In progress"
      )
      DrawMap.labelBox(text, unit.pixelCenter, false)
    })
    
  }
  
}
