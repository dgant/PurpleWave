package Planning.Plans.Macro.Milestones

import Lifecycle.With
import Planning.Composition.Property
import Planning.Composition.UnitMatchers.{UnitMatchAnything, UnitMatcher}
import Planning.Plan

class UnitsExactly(
  initialQuantity:Int = 0,
  initialMatcher:UnitMatcher = UnitMatchAnything) extends Plan {
  
  description.set("Require an exact unit count")
  
  val quantity    = new Property[Int](initialQuantity)
  val unitMatcher = new Property[UnitMatcher](initialMatcher)
  
  override def isComplete: Boolean = With.units.ours.filter(unitMatcher.get.accept).size == quantity.get
}
