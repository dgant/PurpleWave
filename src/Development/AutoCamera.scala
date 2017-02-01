package Development

import Startup.With
import bwapi.Position

object AutoCamera {
  
  var enable = true
  var unit:Option[bwapi.Unit] = None
  var pointOfInterest = new Position(0, 0)

  def render() {
    if (unit == None || ! unit.get.exists()) {
      pickNewUnit()
    }

    With.game.setScreenPosition(
      unit.get.getPosition.getX - 320,
      unit.get.getPosition.getY - 200)
  }

  def focusUnit(newUnit:bwapi.Unit) {
    if (newUnit.getPlayer == With.game.self) {
      unit = Some(newUnit)
    }

    pointOfInterest = unit.get.getPosition
  }

  def pickNewUnit() {
    val units = With.ourUnits
      .filter(unit => unit.isVisible)
      .sortBy(unit => pointOfInterest.getApproxDistance(unit.getPosition))

    if (units.length > 0) {
      unit = Some(units(0))
    }
  }
}
