package Planning.Plans.Macro.UnitCount

import Planning.Composition.Property
import Planning.Plan
import Lifecycle.With
import Planning.Composition.UnitMatchers.{UnitMatchAnything, UnitMatcher}

class UnitCountAtLeast extends Plan {
  
  description.set("Require a minimum unit count")
  
  val unitMatcher = new Property[UnitMatcher](UnitMatchAnything)
  val quantity    = new Property[Int](0)
  
  override def isComplete: Boolean = With.units.ours.filter(unitMatcher.get.accept).size >= quantity.get
}
