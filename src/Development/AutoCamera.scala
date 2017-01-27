package Development

import Startup.With
import bwapi.Position

import scala.collection.JavaConverters._

object AutoCamera {
  var unit:Option[bwapi.Unit] = None
  var pointOfInterest:Position = new Position(0, 0)

  def update() {
    if (unit == None || ! unit.get.exists()) {
      pickNewUnit()
    }

    With.game.setScreenPosition(
      unit.get.getPosition.getX - 320,
      unit.get.getPosition.getY - 240)
  }

  def focusUnit(newUnit:bwapi.Unit) {
    if (newUnit.getPlayer == With.game.self) {
      unit = Some(newUnit)
    }

    pointOfInterest = unit.get.getPosition
  }

  def pickNewUnit() {
    var units = With.game.self.getUnits.asScala
      .filter(unit => unit.isVisible)
      .sortBy(unit => pointOfInterest.getApproxDistance(unit.getPosition))

    if (units.length > 0) {
      unit = Some(units(0))
    }
  }
}
