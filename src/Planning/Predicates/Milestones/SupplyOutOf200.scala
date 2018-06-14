package Planning.Predicates.Milestones

import Lifecycle.With
import Planning.{Predicate, Property}

class SupplyOutOf200(defaultQuantity: Int = 0) extends Predicate {
  
  val quantity = new Property[Int](defaultQuantity)
  
  override def isComplete: Boolean = With.self.supplyUsed >= quantity.get * 2
}
