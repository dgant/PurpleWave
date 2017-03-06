package Plans.Macro.UnitCount

import Plans.Plan
import Startup.With
import Utilities.Property

class SupplyAtLeast extends Plan {
  
  description.set("Require a minimum supply count")
  
  val quantity = new Property[Int](0)
  
  override def isComplete: Boolean = {
    With.game.self.supplyUsed >= quantity.get
  }
}
