package Development.Visualization

import Development.TypeDescriber
import Startup.With
import Types.UnitInfo.ForeignUnitInfo

object VisualizeUnitsForeign {
  
  def render() {
    With.units.enemy.foreach(_drawTrackedUnit)
    With.units.neutral.foreach(_drawTrackedUnit)
  }
  
  def _drawTrackedUnit(trackedUnit:ForeignUnitInfo) {
    if (trackedUnit._possiblyStillThere && ! trackedUnit.visible) {
      With.game.drawCircleMap(
        trackedUnit.pixel,
        trackedUnit.utype.width / 2,
        DrawMap.playerColor(trackedUnit.player))
      DrawMap.label(
        List(TypeDescriber.unit(trackedUnit.utype)),
        trackedUnit.pixel,
        drawBackground = true,
        DrawMap.playerColor(trackedUnit.player))
    }
  }
}
