package Plans.Macro.UnitCount

import Plans.Plan
import Startup.With
import Strategies.UnitMatchers.{UnitMatchAnything, UnitMatcher}
import Utilities.Property

class UnitCountAtLeast extends Plan {
  
  description.set("Require a minimum unit count")
  
  val unitMatcher = new Property[UnitMatcher](UnitMatchAnything)
  val quantity    = new Property[Int](0)
  
  override def isComplete: Boolean = With.units.ours.filter(unitMatcher.get.accept).size >= quantity.get
}
