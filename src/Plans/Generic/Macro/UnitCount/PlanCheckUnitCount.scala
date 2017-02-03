package Plans.Generic.Macro.UnitCount

import Plans.Plan
import Startup.With
import Strategies.UnitMatchers.{UnitMatchAnything, UnitMatcher}

class PlanCheckUnitCount extends Plan {
  var unitMatcher:UnitMatcher = UnitMatchAnything
  var minimum = 0
  
  override def isComplete(): Boolean = {
    With.ourUnits.filter(unitMatcher.accept).size >= minimum
  }
  
}
