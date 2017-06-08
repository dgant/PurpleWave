package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Rendering.DrawMap
import Lifecycle.With
import ProxyBwapi.UnitInfo.ForeignUnitInfo

object VisualizeUnitsForeign {
  
  def render() {
    With.units.enemy.foreach(drawTrackedUnit)
    With.units.neutral.foreach(drawTrackedUnit)
  }
  
  private def drawTrackedUnit(unit:ForeignUnitInfo) {
    if ( ! With.viewport.contains(unit.pixelCenter)) return
    if (unit.possiblyStillThere) {
      if (unit.effectivelyCloaked || ! unit.visible) {
        val color = if (unit.likelyStillThere) unit.player.colorDark else unit.player.colorMidnight
        DrawMap.circle(
          unit.pixelCenter,
          unit.unitClass.width / 2,
          unit.player.colorDark)
        DrawMap.label(
          unit.unitClass.toString,
          unit.pixelCenter,
          drawBackground = true,
          unit.player.colorDark)
      } else {
        val targetUnit = unit.target.orElse(unit.orderTarget)
        if (targetUnit.nonEmpty) {
          DrawMap.line(unit.pixelCenter, targetUnit.get.pixelCenter, unit.player.colorNeon)
        }
        else {
          val targetPosition = unit.targetPixel.orElse(unit.orderTargetPixel)
          if (targetPosition.nonEmpty) {
            DrawMap.line(unit.pixelCenter, targetPosition.get, unit.player.colorDark)
          }
        }
      }
    }
  }
}
