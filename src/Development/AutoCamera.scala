package Development

import Startup.BotListener
import bwapi.Position
import scala.collection.JavaConverters._

object AutoCamera {
  var unit:Option[bwapi.Unit] = None
  var pointOfInterest:Position = new Position(0, 0)

  def update() {
    if (unit == None || ! unit.get.exists()) {
      pickNewUnit()
    }

    BotListener.bot.get.game.setScreenPosition(
      unit.get.getPosition.getX - 320,
      unit.get.getPosition.getY - 240)
  }

  def focusUnit(newUnit:bwapi.Unit) {
    unit = Some(newUnit)
    pointOfInterest = unit.get.getPosition
  }

  def pickNewUnit() {
    var units = BotListener.bot.get.self.getUnits.asScala
      .filter(unit => unit.isVisible)
      .sortBy(unit => pointOfInterest.getApproxDistance(unit.getPosition))

    if (units.length > 0) {
      unit = Some(units(0))
    }
  }
}
