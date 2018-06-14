package Planning.Plans.Predicates.Milestones

import Lifecycle.With
import Planning.Composition.Property
import Planning.Predicate

class SupplyOutOf200(defaultQuantity: Int = 0) extends Predicate {
  
  description.set("Require a minimum supply count")
  
  val quantity = new Property[Int](defaultQuantity)
  
  override def isComplete: Boolean = With.self.supplyUsed >= quantity.get * 2
}
