package Plans.Generic.Macro.UnitAtLocation

import Plans.Generic.Allocation.PlanAcquireUnitsExactly
import Plans.Generic.Compound.PlanCompleteAllInSerial
import Strategies.PositionFinders.PositionFinder
import Strategies.UnitMatchers.UnitMatcher

class PlanFulfillUnitAtLocation(
  val unitMatcher: UnitMatcher,
  val positionFinder: PositionFinder)
    extends PlanCompleteAllInSerial {
  
  val _units = new PlanAcquireUnitsExactly(unitMatcher)
  kids = List(_units)
  
  override def isComplete(): Boolean = { false }
  
  override def execute(): Unit = {
    super.execute()
    
    if (_units.isComplete) {
      _units.units.foreach(unit =>
        positionFinder.find.foreach(position =>
          unit.move(position.toPosition)))
    }
  }
}
