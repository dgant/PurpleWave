package Plans.Generic.Macro.UnitCount

import Plans.Plan
import Startup.With
import Traits.{TraitSettableQuantity, TraitSettableUnitMatcher}

class PlanCheckUnitCountAtLeast
  extends Plan
  with TraitSettableUnitMatcher
  with TraitSettableQuantity {
  
  override def isComplete(): Boolean = {
    With.ourUnits
      .filter(getUnitMatcher.accept)
      .size >= getQuantity
  }
  
}
