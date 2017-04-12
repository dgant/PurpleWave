package Debugging.Visualizations.Views

import Debugging.Visualizations.Rendering.DrawMap
import ProxyBwapi.UnitInfo.ForeignUnitInfo
import Lifecycle.With

object VisualizeUnitsForeign {
  
  def render() {
    With.units.enemy.foreach(drawTrackedUnit)
    With.units.neutral.foreach(drawTrackedUnit)
  }
  
  private def drawTrackedUnit(trackedUnit:ForeignUnitInfo) {
    if (trackedUnit.possiblyStillThere) {
      if (((trackedUnit.cloaked || trackedUnit.burrowed) && ! trackedUnit.detected) || ! trackedUnit.visible) {
      DrawMap.circle(
        trackedUnit.pixelCenter,
        trackedUnit.unitClass.width / 2,
        trackedUnit.player.colorDark)
      DrawMap.label(
        trackedUnit.unitClass.toString,
        trackedUnit.pixelCenter,
        drawBackground = true,
        trackedUnit.player.colorDark)
      }
    }
  }
}
