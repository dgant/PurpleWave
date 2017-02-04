package Plans.Generic.Macro.UnitAtLocation

import Plans.Plan
import Startup.With
import Strategies.PositionFinders.{PositionCenter, PositionFinder}
import Strategies.UnitMatchers.{UnitMatchAnything, UnitMatcher}
import Traits.Property

class PlanCheckUnitAtLocation extends Plan {
  
  val positionFinder  = new Property[PositionFinder](new PositionCenter)
  val unitMatcher     = new Property[UnitMatcher](UnitMatchAnything)
  val range           = new Property[Integer](32)
  
  override def isComplete: Boolean = {
    With.ourUnits
      .filter(unitMatcher.get.accept)
      .exists(unit => positionFinder.get.find.exists(
        _.toPosition.getDistance(unit.getPosition) < range.get))
  }
}
