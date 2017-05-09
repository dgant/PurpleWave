package Planning.Plans.Macro.Milestones

import Planning.Composition.Property
import Planning.Plan
import Lifecycle.With
import Planning.Composition.UnitMatchers.{UnitMatchAnything, UnitMatcher}

class UnitsAtLeast(
  initialQuantity:Int = 0,
  initialMatcher:UnitMatcher = UnitMatchAnything) extends Plan {
  
  description.set("Require a minimum unit count")
  
  val quantity    = new Property[Int](initialQuantity)
  val unitMatcher = new Property[UnitMatcher](initialMatcher)
  
  override def isComplete: Boolean = With.units.ours.filter(unitMatcher.get.accept).size >= quantity.get
}
