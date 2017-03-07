package Plans.Macro.Automatic

import Plans.Macro.Build.TrainUnit
import Startup.With
import bwapi.{UnitType, UpgradeType}

class TrainGatewayUnitsContinuously extends AbstractBuildContinuously[TrainUnit] {
  
  override def _createPlan:TrainUnit = {
    val haveLegSpeed = With.game.self.getUpgradeLevel(UpgradeType.Leg_Enhancements) > 0
    val numberOfZealots = With.units.ours.filter(_.utype == UnitType.Protoss_Zealot).size
    val numberOfDragoons = With.units.ours.filter(_.utype == UnitType.Protoss_Dragoon).size
    val numberOfDarkTemplar = With.units.ours.filter(_.utype == UnitType.Protoss_Dark_Templar).size
    val canBuildDragoon = With.units.ours.exists(_.utype == UnitType.Protoss_Cybernetics_Core) && With.game.self.gas >= UnitType.Protoss_Dragoon.gasPrice
    val canBuildDarkTemplar = With.units.ours.exists(_.utype == UnitType.Protoss_Templar_Archives) && With.game.self.gas >= UnitType.Protoss_Dark_Templar.gasPrice
    
    new TrainUnit(
      if(canBuildDarkTemplar && numberOfDarkTemplar < 2) {
        UnitType.Protoss_Dark_Templar
      }
      else if (canBuildDragoon && (numberOfDragoons * 2 < numberOfZealots || ! haveLegSpeed )) {
        UnitType.Protoss_Dragoon
      }
      else {
        UnitType.Protoss_Zealot
      })
  }
  
  override def _additionalPlansRequired:Int = {
    Math.max(0, With.units.ours.filter(_.complete).filter(_.utype == UnitType.Protoss_Gateway).size - _currentBuilds.size)
  }
}
