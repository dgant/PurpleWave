package Plans.Generic.Macro.UnitCount

import Plans.Plan
import Startup.With
import Strategies.UnitMatchers.{UnitMatchAnything, UnitMatcher}
import Traits.Property

class PlanCheckUnitCountAtLeast extends Plan {
  
  val unitMatcher = new Property[UnitMatcher](UnitMatchAnything)
  val quantity    = new Property[Integer](0)
  
  override def isComplete: Boolean = {
    With.ourUnits.filter(unitMatcher.get.accept).size >= quantity.get
  }
  
}
