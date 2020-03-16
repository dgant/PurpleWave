package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Information.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Mathematics.Points.Pixel
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.ForeignUnitInfo

object ShowUnitsEnemy extends View {
  
  var showSiegeRadius = true
  var showFogged      = true
  var showCountdown   = true
  
  override def renderMap() {
    With.units.enemy.foreach(renderUnit)
  }
  
  private def renderUnit(unit: ForeignUnitInfo) {
    
    val color = unit.color
      
    if (showFogged) {
      if ( ! unit.visible || unit.effectivelyCloaked) {
        DrawMap.circle(unit.pixelCenter, unit.unitClass.dimensionMin / 2, color)
        DrawMap.label(unit.unitClass.toString, unit.pixelCenter, drawBackground = true, color)
      }
    }
    if (showSiegeRadius) {
      if (unit.is(Terran.SiegeTankSieged)) {
        With.game.drawCircleMap(
          unit.pixelCenter.bwapi,
          (unit.pixelRangeGround + unit.unitClass.radialHypotenuse).toInt,
          Colors.DarkOrange)
      }
    }
    if (showCountdown) {
      if ( ! unit.complete && unit.remainingCompletionFrames > 0) {
        val time = new GameTime(unit.remainingCompletionFrames)
        val pctHp = unit.hitPoints.toDouble / unit.unitClass.maxHitPoints
        DrawMap.labelBox(Array(time.toString, "%02f".format(pctHp) + "%"), Pixel(unit.x, unit.bottom), drawBackground = true, unit.color)
      }
    }
  }
}
