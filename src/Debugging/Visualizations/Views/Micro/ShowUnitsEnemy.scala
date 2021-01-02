package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Lifecycle.With
import Mathematics.Points.Pixel
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.ForeignUnitInfo
import Utilities.GameTime

object ShowUnitsEnemy extends View {
  
  var showSiegeRadius = true
  var showFogged      = true
  var showCountdown   = true
  var showTargets     = true
  
  override def renderMap() {
    With.units.enemy.foreach(renderUnit)
  }
  
  private def renderUnit(unit: ForeignUnitInfo) {
    
    val color = unit.teamColor

    if (showTargets && unit.visible) {
      ShowUnitsFriendly.renderTargets(unit)
    }
      
    if (showFogged) {
      if ( ! unit.visible || unit.effectivelyCloaked) {
        val radius = unit.unitClass.dimensionMin / 2
        DrawMap.circle(unit.pixel, radius, color)
        if (unit.pixel != unit.pixelCenterObserved) {
          DrawMap.circle(unit.pixelCenterObserved, radius, color)
          DrawMap.line(
            unit.pixel.project(unit.pixelCenterObserved, radius),
            unit.pixelCenterObserved.project(unit.pixel, radius),
            color)
        }
        DrawMap.label(unit.unitClass.toString, unit.pixel, drawBackground = true, color)
      }
    }
    if (showSiegeRadius) {
      if (unit.is(Terran.SiegeTankSieged)) {
        With.game.drawCircleMap(
          unit.pixel.bwapi,
          (unit.pixelRangeGround + unit.unitClass.radialHypotenuse).toInt,
          Colors.DarkOrange)
      }
    }
    if (showCountdown) {
      if ( ! unit.complete && unit.remainingCompletionFrames > 0) {
        val time = new GameTime(unit.remainingCompletionFrames)
        val pctHp = unit.hitPoints.toDouble / unit.unitClass.maxHitPoints
        DrawMap.labelBox(Array(time.toString, "%02f".format(pctHp) + "%"), Pixel(unit.x, unit.bottom), drawBackground = true, unit.teamColor)
      }
    }
  }
}
