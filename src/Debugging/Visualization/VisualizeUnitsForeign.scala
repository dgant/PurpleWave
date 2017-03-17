package Debugging.Visualization

import Startup.With
import ProxyBwapi.UnitInfo.ForeignUnitInfo

object VisualizeUnitsForeign {
  
  def render() {
    With.units.enemy.foreach(drawTrackedUnit)
    With.units.neutral.foreach(drawTrackedUnit)
  }
  
  private def drawTrackedUnit(trackedUnit:ForeignUnitInfo) {
    if (trackedUnit._possiblyStillThere && ! trackedUnit.visible) {
      DrawMap.circle(
        trackedUnit.pixelCenter,
        trackedUnit.unitClass.width / 2,
        DrawMap.playerColor(trackedUnit.player))
      DrawMap.label(
        trackedUnit.unitClass.toString,
        trackedUnit.pixelCenter,
        drawBackground = true,
        DrawMap.playerColor(trackedUnit.player))
    }
  }
}
