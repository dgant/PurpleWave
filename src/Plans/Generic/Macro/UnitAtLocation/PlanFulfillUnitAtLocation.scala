package Plans.Generic.Macro.UnitAtLocation

import Plans.Generic.Allocation.PlanAcquireUnitsExactly
import Plans.Plan
import Traits.{TraitSettablePositionFinder, TraitSettableUnitMatcher, TraitSettableUnitPreference}

class PlanFulfillUnitAtLocation
    extends Plan
    with TraitSettableUnitMatcher
    with TraitSettableUnitPreference
    with TraitSettablePositionFinder {
  
  var _units = new PlanAcquireUnitsExactly
  
  override def children(): Iterable[Plan] = List(_units)
  override def isComplete(): Boolean = { false }
  
  final override def onFrame(): Unit = {
    _units.setUnitMatcher(getUnitMatcher)
    _units.setUnitPreference(getUnitPreference)
    _units.onFrame()
    
    if (_units.isComplete) {
      _units.units.foreach(unit =>
        getPositionFinder.find.foreach(position =>
          unit.move(position.toPosition)))
    }
  }
}
