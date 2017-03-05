package Plans.Macro.Automatic

import Plans.Macro.Build.TrainUnit
import Startup.With
import bwapi.UnitType

class TrainContinuously(unitType:UnitType) extends AbstractBuildContinuously[TrainUnit] {
  
  override def _additionalPlansRequired: Int =
    Math.max(
      0,
      With.units.ours
        .filter(_.complete)
        .count(_.utype == unitType.whatBuilds.first) * unitType.whatBuilds.second - _currentBuilds.size)
  
  override def _createPlan:TrainUnit = new TrainUnit(unitType)
}
