package Plans.Macro.Automatic

import Plans.Macro.Build.TrainUnit
import Startup.With
import bwapi.UnitType

class BuildGatewayUnitsContinuously extends AbstractBuildContinuously[TrainUnit] {
  
  override def _buildPlan:TrainUnit = {
    new TrainUnit(
      if (With.units.ours.exists(_.utype == UnitType.Protoss_Templar_Archives)
        && With.game.self.gas >= UnitType.Protoss_Dark_Templar.gasPrice
        && With.units.ours.filter(_.utype == UnitType.Protoss_Dark_Templar).size < 2) {
        UnitType.Protoss_Dark_Templar
      }
      else if (With.units.ours.exists(_.utype == UnitType.Protoss_Cybernetics_Core)
        && With.game.self.gas >= UnitType.Protoss_Dragoon.gasPrice) {
        UnitType.Protoss_Dragoon
      }
      else {
        UnitType.Protoss_Zealot
      })
  }
  
  override def _additionalPlansRequired:Int = {
    Math.max(0, With.units.ours.filter(_.utype == UnitType.Protoss_Gateway).size - _currentBuilds.size)
  }
}
