package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Macro.BuildRequests.RequestAtLeast
import Planning.Plan
import ProxyBwapi.UnitClass.UnitClass

class TrainContinuously(unitClass: UnitClass, maximum: Int = Int.MaxValue) extends Plan {
  
  description.set("Continuously train " + unitClass)
  
  override def onUpdate() {
    if ( ! canBuild) return
    
    With.scheduler.request(
      this,
      RequestAtLeast(
        List(
          maximum,
          maxDesirable,
          buildCapacity + With.units.ours.count(unit => unit.aliveAndComplete && unit.unitClass == unitClass))
        .min,
        unitClass))
  }
  
  protected def canBuild: Boolean = {
    unitClass.buildTechEnabling.forall(With.self.hasTech) &&
    unitClass.buildUnitsEnabling.forall(unitClass => With.units.ours.exists(unit => unit.aliveAndComplete && unit.is(unitClass))) &&
    unitClass.buildUnitsBorrowed.forall(unitClass => With.units.ours.exists(unit => unit.aliveAndComplete && unit.is(unitClass)))
  }
  
  protected def buildCapacity: Int = {
    Vector(
      With.units.ours.count(_.is(unitClass.whatBuilds._1)),
      if (unitClass.supplyRequired == 0) 400 else (400 - With.self.supplyUsed) / unitClass.supplyRequired
    ).min
  }
  
  protected def maxDesirable: Int = {
    Int.MaxValue
  }
}
