package Plans.Generic.Macro.UnitAtLocation

import Plans.Plan
import Startup.With
import Strategies.PositionFinders.PositionFinder
import Strategies.UnitMatchers.UnitMatcher

class PlanCheckUnitAtLocation(
  val unitMatcher: UnitMatcher,
  val positionFinder: PositionFinder,
  val leashRange:Integer = 128) extends Plan {
  
  override def isComplete(): Boolean = {
    val position = positionFinder.find
    With.ourUnits
      .filter(unitMatcher.accept)
      .exists(unit => position.exists(_.toPosition.getDistance(unit.getPosition) < leashRange))
  }
}
