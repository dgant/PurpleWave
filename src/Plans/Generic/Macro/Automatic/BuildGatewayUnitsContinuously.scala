package Plans.Generic.Macro.Automatic

import Plans.Generic.Macro.TrainUnit
import Startup.With
import bwapi.UnitType

class BuildGatewayUnitsContinuously extends AbstractBuildContinuously[TrainUnit] {
  
  override def _buildPlan:TrainUnit = {
    new TrainUnit(
      if (With.ourUnits.exists(_.getType == UnitType.Protoss_Cybernetics_Core) && With.game.self.gas >= 50) {
        UnitType.Protoss_Dragoon
      }
      else {
        UnitType.Protoss_Zealot
      })
  }
  
  override def _additionalPlansRequired:Int = {
    Math.max(0, With.ourUnits.filter(_.getType == UnitType.Protoss_Gateway).size - _currentBuilds.size)
  }
}
