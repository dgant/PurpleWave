package Planning.Plans.Macro.Automatic

import Startup.With
import bwapi.UnitType

class TrainContinuously(var unitTypeToTrain:UnitType = UnitType.None) extends AbstractBuildContinuously {
  
  override protected def unitType:UnitType = unitTypeToTrain
  override protected def totalRequiredRecalculate:Int = {
    val now = With.units.ours.count(_.utype == unitTypeToTrain)
    val capacity = List(
      With.units.ours.filter(_.complete).count(_.utype == unitTypeToTrain.whatBuilds.first) * unitTypeToTrain.whatBuilds.second,
      With.self.minerals / Math.max(1, unitTypeToTrain.mineralPrice),
      With.self.gas / Math.max(1, unitTypeToTrain.gasPrice)
    )
    .min
    now + capacity
  }
    
}
