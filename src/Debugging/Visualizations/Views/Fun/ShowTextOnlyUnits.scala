package Debugging.Visualizations.Views.Fun

import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Lifecycle.With

object ShowTextOnlyUnits extends View {
  
  override def renderMap() {
    
    With.units.all.filter(_.visible).filter(u => u.complete || u.unitClass.isBuilding).foreach(unit => {
      val color = unit.player.colorDark
      val text = Vector(
        unit.player.name,
        unit.unitClass.toString,
        if (unit.player == With.neutral) "" else unit.totalHealth.toString + "/" + unit.unitClass.maxTotalHealth,
        if (unit.complete) "" else "In progress"
      )
      DrawMap.labelBox(text, unit.pixel, false)
    })
    
  }
  
}
