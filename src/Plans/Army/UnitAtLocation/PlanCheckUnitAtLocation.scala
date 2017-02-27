package Plans.Army.UnitAtLocation

import Plans.Plan
import Startup.With
import Strategies.PositionFinders.{PositionCenter, PositionFinder}
import Strategies.UnitMatchers.{UnitMatchAnything, UnitMatcher}
import Utilities.Property

class PlanCheckUnitAtLocation extends Plan {
  
  val quantity        = new Property[Int](1)
  val positionFinder  = new Property[PositionFinder](new PositionCenter)
  val unitMatcher     = new Property[UnitMatcher](UnitMatchAnything)
  val range           = new Property[Int](32)
  
  override def isComplete: Boolean = {
    With.units.ours
      .filter(unitMatcher.get.accept)
      .filter(unit =>
        positionFinder.get.find.exists(position =>
          position.toPosition.getDistance(unit.position) < range.get))
      .size >= quantity.get
  }
}
