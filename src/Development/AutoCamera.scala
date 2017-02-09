package Development

import Startup.With
import bwapi.Position

object AutoCamera {
  
  var enabled = true
  var unit:Option[bwapi.Unit] = None
  var pointOfInterest = new Position(0, 0)

  def onFrame() {
    if ( ! enabled) {
      return
    }
    
    if (unit == None || ! unit.get.exists()) {
      pickNewUnit()
    }

    With.game.setScreenPosition(
      unit.get.getPosition.getX - 320,
      unit.get.getPosition.getY - 200)
  }

  def focusUnit(newUnit:bwapi.Unit) {
    if ( ! enabled) {
      return
    }
    
    if (newUnit.exists && newUnit.getPlayer == With.game.self) {
      unit = Some(newUnit)
      pointOfInterest = unit.get.getPosition
    }
  }

  def pickNewUnit() {
    if ( ! enabled) {
      return
    }
    
    val units = With.ourUnits
      .filter(unit => unit.isVisible)
      .sortBy(unit => pointOfInterest.getApproxDistance(unit.getPosition))

    if (units.length > 0) {
      unit = Some(units(0))
    }
  }
}
