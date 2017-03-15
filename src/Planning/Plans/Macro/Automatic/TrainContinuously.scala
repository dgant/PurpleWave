package Planning.Plans.Macro.Automatic

import Startup.With
import bwapi.UnitType

class TrainContinuously(var unitType:UnitType = UnitType.None) extends AbstractBuildContinuously {
  
  override def _unitType:UnitType = unitType
  override def _totalRequired:Int = {
    val now = With.units.ours.count(_.utype == unitType)
    val capacity = List(
      With.units.ours.filter(_.complete).count(_.utype == unitType.whatBuilds.first) * unitType.whatBuilds.second,
      With.self.minerals / Math.max(1, unitType.mineralPrice),
      With.self.gas / Math.max(1, unitType.gasPrice)
    )
    .min
    now + capacity
  }
    
}
