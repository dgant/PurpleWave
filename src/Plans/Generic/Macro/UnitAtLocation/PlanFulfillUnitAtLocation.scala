package Plans.Generic.Macro.UnitAtLocation

import Plans.Generic.Allocation.{LockUnits, LockUnitsNobody}
import Plans.Plan
import Strategies.PositionFinders.{PositionCenter, PositionFinder}
import Traits.Property

class PlanFulfillUnitAtLocation extends Plan {
  
  val unitPlan        = new Property[LockUnits](LockUnitsNobody)
  val positionFinder  = new Property[PositionFinder](new PositionCenter)
  
  override def getChildren: Iterable[Plan] = { List (unitPlan.get) }
  
  final override def onFrame(): Unit = {
    unitPlan.get.onFrame()
    unitPlan.get.units.foreach(unit =>
        positionFinder.get.find.foreach(position =>
          unit.move(position.toPosition)))
  }
}
