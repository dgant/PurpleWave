package Planning.Plans.Macro.Automatic.Continuous

import Lifecycle.With
import Macro.BuildRequests.RequestUnitAtLeast
import Planning.Plan
import ProxyBwapi.UnitClass.UnitClass

class TrainContinuously(unitClass: UnitClass, maximum:Int = Int.MaxValue) extends Plan {
  
  override def onUpdate() {
    if ( ! canBuild) return
    
    With.scheduler.request(this, Vector(
      RequestUnitAtLeast(
        List(
          maximum,
          maxDesirable,
          buildCapacity + With.units.ours.count(unit => unit.aliveAndComplete && unit.unitClass == unitClass))
        .min,
        unitClass)))
  }
  
  protected def canBuild:Boolean = {
    //TODO: Do we trust this?
    With.game.canMake(unitClass.baseType)
  }
  
  protected def buildCapacity:Int = {
    Vector(
      With.units.ours.count(_.is(unitClass.whatBuilds._1)),
      if (unitClass.supplyRequired == 0) 400 else (400 - With.self.supplyUsed) / unitClass.supplyRequired
      //if (unitClass.mineralPrice <= 0) Int.MaxValue else With.minerals / unitClass.mineralPrice,
      //if (unitClass.gasPrice     <= 0) Int.MaxValue else With.minerals / unitClass.gasPrice
    ).min
  }
  
  protected def maxDesirable:Int = {
    Int.MaxValue
  }
  
}
