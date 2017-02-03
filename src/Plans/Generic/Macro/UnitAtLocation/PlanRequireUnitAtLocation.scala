package Plans.Generic.Macro.UnitAtLocation

import Plans.Generic.Compound.PlanFulfillRequirements
import Strategies.PositionFinders.PositionFinder
import Strategies.UnitMatchers.UnitMatcher

class PlanRequireUnitAtLocation(
  val unitMatcher: UnitMatcher,
  val positionFinder: PositionFinder,
  val leashRange:Integer = 32)
    extends PlanFulfillRequirements {
  
  requirement = new PlanCheckUnitAtLocation(unitMatcher, positionFinder, leashRange)
  fulfiller = new PlanFulfillUnitAtLocation(unitMatcher, positionFinder)
}
