package Development.Overlay

import Development.TypeDescriber
import Startup.With
import Types.UnitInfo.ForeignUnitInfo

object DrawTrackedUnits {
  
  def draw() {
    With.units.enemy.foreach(_drawTrackedUnit)
    With.units.neutral.foreach(_drawTrackedUnit)
  }
  
  def _drawTrackedUnit(trackedUnit:ForeignUnitInfo) {
    if (trackedUnit._possiblyStillThere && ! trackedUnit.visible) {
      With.game.drawCircleMap(
        trackedUnit.pixel,
        trackedUnit.utype.width / 2,
        Draw.playerColor(trackedUnit.player))
      Draw.label(
        List(TypeDescriber.unit(trackedUnit.utype)),
        trackedUnit.pixel,
        drawBackground = true,
        Draw.playerColor(trackedUnit.player))
    }
  }
}
