package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Lifecycle.With
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.ForeignUnitInfo

object ShowUnitsForeign extends View {
  
  var showSiegeRadius = true
  var showFogged      = true
  var showTargets     = true
  var showSaturation  = true
  
  override def renderMap() {
    With.units.enemy.foreach(drawTrackedUnit)
    With.units.neutral.foreach(drawTrackedUnit)
    renderSaturation()
  }
  
  private def drawTrackedUnit(unit: ForeignUnitInfo) {
    if ( ! With.viewport.contains(unit.pixelCenter)) return
    

    
    val color = unit.color
      
    if (showFogged) {
      if ( ! unit.visible) {
        DrawMap.circle(unit.pixelCenter, unit.unitClass.width / 2, color)
        DrawMap.label(unit.unitClass.toString, unit.pixelCenter, drawBackground = true, color)
      }
    }
    if (showSiegeRadius) {
      if (unit.is(Terran.SiegeTankSieged)) {
        With.game.drawCircleMap(
          unit.pixelCenter.bwapi,
          (unit.pixelRangeGround + unit.unitClass.radialHypotenuse).toInt,
          color)
      }
    }
    if (showTargets) {
      if (unit.targetPixel.isDefined) {
        DrawMap.line(unit.pixelCenter, unit.targetPixel.get, unit.player.colorMedium)
      }
      if (unit.target.isDefined) {
        DrawMap.line(unit.pixelCenter, unit.target.get.pixelCenter, unit.player.colorNeon)
      }
    }
  }
  
  def renderSaturation() {
    if ( ! showSaturation) return
    
    val resourcesSaturated = With.units.ours
      .filter(unit =>
        unit.velocityX == 0 &&
        unit.velocityY == 0 &&
        unit.target.exists(_.unitClass.isResource))
      .flatMap(_.target)
    
    With.geography.ourBases
      .flatMap(base => base.resources)
      .filterNot(resourcesSaturated.contains)
      .foreach(resource => {
        DrawMap.circle(resource.pixelCenter, 6, resource.player.colorDark, solid = true)
        DrawMap.label(":(", resource.pixelCenter.add(3, -1))
      })
  }
}
