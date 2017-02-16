package Plans.Generic.Army.UnitAtLocation

import Plans.Plan
import Startup.With
import Strategies.PositionFinders.{PositionCenter, PositionFinder}
import Strategies.UnitMatchers.{UnitMatchAnything, UnitMatcher}
import Types.Property

class PlanCheckUnitAtLocation extends Plan {
  
  val quantity        = new Property[Integer](1)
  val positionFinder  = new Property[PositionFinder](new PositionCenter)
  val unitMatcher     = new Property[UnitMatcher](UnitMatchAnything)
  val range           = new Property[Integer](32)
  
  override def isComplete: Boolean = {
    With.ourUnits
      .filter(unitMatcher.get.accept)
      .filter(unit =>
        positionFinder.get.find.exists(position =>
          position.toPosition.getDistance(unit.getPosition) < range.get))
      .size >= quantity.get
  }
}
