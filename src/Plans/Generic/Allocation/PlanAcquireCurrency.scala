package Plans.Generic.Allocation

import Startup.With
import Plans.Plan
import Traits.TraitSettableSatisfaction

class PlanAcquireCurrency
  extends Plan
  with TraitSettableSatisfaction {
  
  var minerals = 0
  var gas = 0
  var supply = 0
  var isSpent = false
  
  override def isComplete(): Boolean = { getSatisfaction }
  
  override def onFrame() {
    With.bank.add(this)
  }
}
