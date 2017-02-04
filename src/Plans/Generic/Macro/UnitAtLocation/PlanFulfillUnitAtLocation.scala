package Plans.Generic.Macro.UnitAtLocation

import Plans.Generic.Allocation.LockUnits
import Plans.Plan
import Strategies.PositionFinders.{PositionCenter, PositionFinder}
import Traits.Property

class PlanFulfillUnitAtLocation extends Plan {
  
  val unitPlan        = new Property[Option[LockUnits]](None)
  val positionFinder  = new Property[PositionFinder](new PositionCenter)
  
  override def getChildren: Iterable[Plan] = unitPlan.get
  
  final override def onFrame(): Unit = {
    unitPlan.get.foreach(plan => {
      plan.onFrame()
      plan.units.foreach(unit =>
        positionFinder.get.find.foreach(position =>
          unit.move(position.toPosition)))})
  }
}
