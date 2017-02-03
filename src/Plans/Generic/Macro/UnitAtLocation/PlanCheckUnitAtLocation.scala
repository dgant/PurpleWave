package Plans.Generic.Macro.UnitAtLocation

import Plans.Plan
import Startup.With
import Traits.{TraitSettablePositionFinder, TraitSettableRange, TraitSettableUnitMatcher}

class PlanCheckUnitAtLocation
  extends Plan
  with TraitSettablePositionFinder
  with TraitSettableUnitMatcher
  with TraitSettableRange {
  
  override def isComplete(): Boolean = {
    val position = getPositionFinder.find
    With.ourUnits
      .filter(getUnitMatcher.accept)
      .exists(unit => position.exists(
        _.toPosition.getDistance(unit.getPosition) < getRange))
  }
}
