package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.DebugView
import Lifecycle.With
import Mathematics.Points.Pixel
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.ForeignUnitInfo
import Utilities.Time.Frames

object ShowUnitsEnemy extends DebugView {
  
  var showSiegeRadius = true
  var showFogged      = true
  var showCountdown   = true
  
  override def renderMap(): Unit = {
    With.units.enemy.foreach(renderUnit)
  }
  
  private def renderUnit(unit: ForeignUnitInfo): Unit = {
    
    val color = unit.teamColor
      
    if (showFogged) {
      if ( ! unit.visible || unit.effectivelyCloaked) {
        val radius = unit.unitClass.dimensionMin / 2
        DrawMap.circle(unit.pixel, radius, color, solid = true)
        if (unit.pixel != unit.pixelObserved) {
          DrawMap.circle(unit.pixelObserved, radius, color)
          DrawMap.line(
            unit.pixel.project(unit.pixelObserved, radius),
            unit.pixelObserved.project(unit.pixel, radius),
            color)
        }
        DrawMap.label(unit.unitClass.toString, unit.pixel, drawBackground = true, color)
      }
    }
    if (showSiegeRadius) {
      if (Terran.SiegeTankSieged(unit)) {

        With.game.drawCircleMap(
          unit.pixel.bwapi,
          (unit.pixelRangeGround + unit.unitClass.radialHypotenuse).toInt,
          Colors.DarkOrange)
      }
    }
    if (showCountdown) {
      if ( ! unit.complete && unit.remainingCompletionFrames > 0) {
        val timeLeft  = Frames(unit.remainingCompletionFrames)
        val timeEnd   = Frames(unit.completionFrame)
        val pctHp     = unit.hitPoints.toDouble / unit.unitClass.maxHitPoints
        DrawMap.labelBox(
          Array(
            f"$timeLeft ($timeEnd)",
            "%02f".format(pctHp) + "%"),
          Pixel(unit.x, unit.bottomExclusive), drawBackground = true, unit.teamColor)
      }
    }
  }
}
