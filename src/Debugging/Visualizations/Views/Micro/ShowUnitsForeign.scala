package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Lifecycle.With
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.ForeignUnitInfo

object ShowUnitsForeign extends View {
  
  override def renderMap() {
    With.units.enemy.foreach(drawTrackedUnit)
    With.units.neutral.foreach(drawTrackedUnit)
    renderSaturation()
  }
  
  private def drawTrackedUnit(unit: ForeignUnitInfo) {
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
        color)
      DrawMap.label(
        unit.unitClass.toString,
        unit.pixelCenter,
        drawBackground = true,
        color)
    }
    
    if (unit.is(Terran.SiegeTankSieged)) {
      DrawMap.circle(
        unit.pixelCenter,
        (unit.pixelRangeGround + unit.unitClass.radialHypotenuse).toInt,
        unit.player.colorNeon)
    }
  }
  
  def renderSaturation() {
    val resourcesSaturated = With.units.ours
      .filter(unit =>
        unit.velocityX == 0 &&
        unit.velocityY == 0 &&
        unit.target.exists(_.unitClass.isResource))
      .flatMap(_.target)
  
    With.game.setTextSize(bwapi.Text.Size.Enum.Large)
    With.geography.ourBases
      .flatMap(base => base.resources)
      .filterNot(resourcesSaturated.contains)
      .foreach(resource => {
        DrawMap.circle(resource.pixelCenter, 12, Colors.MidnightTeal, solid = true)
        DrawMap.label(":(", resource.pixelCenter.add(2, -5))
      })
    With.game.setTextSize(bwapi.Text.Size.Enum.Small)
  }
}
