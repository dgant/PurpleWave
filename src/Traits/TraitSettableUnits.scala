package Traits

import Plans.Generic.Allocation.PlanAcquireUnits

trait TraitSettableUnits {
  var _units:Option[PlanAcquireUnits] = None
  
  def getUnits:Option[PlanAcquireUnits] = {
    _units
  }
  
  def setUnits(value:PlanAcquireUnits) {
    _units = Some(value)
  }
}
