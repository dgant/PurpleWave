package Plans.Generic.Macro.UnitAtLocation

import Plans.Generic.Compound.AbstractPlanFulfillRequirements
import Plans.Plan
import Traits.{TraitSettablePositionFinder, TraitSettableRange, TraitSettableUnitMatcher, TraitSettableUnitPreference}

class PlanRequireUnitAtLocation
    extends AbstractPlanFulfillRequirements
    with TraitSettablePositionFinder
    with TraitSettableUnitMatcher
    with TraitSettableUnitPreference
    with TraitSettableRange {
  
  var _check = new PlanCheckUnitAtLocation
  var _fulfill = new PlanFulfillUnitAtLocation
  
  override def _getChecker:Plan = {
    _check.setPositionFinder(getPositionFinder)
    _check.setUnitMatcher(getUnitMatcher)
    _check.setRange(getRange)
    _check
  }
  
  override def _getFulfiller() :Plan = {
    _fulfill.setPositionFinder(getPositionFinder)
    _fulfill.setUnitMatcher(getUnitMatcher)
    _fulfill.setUnitPreference(getUnitPreference)
    _fulfill
  }
}
