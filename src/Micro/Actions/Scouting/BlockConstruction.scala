package Micro.Actions.Scouting

import Micro.Actions.Action
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object BlockConstruction extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    true
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    
  }
  
  def blockableBuilders(unit: FriendlyUnitInfo): Iterable[UnitInfo] = {
    List.empty
    /*
    unit.matchups.targets.filter(builder =>
      builder.unitClass.isWorker &&
      (
        builder.command.exists(_.getUnitCommandType == UnitCommandType.Build) //||
        //builder.targetPixel.exists(targetPixel => )
      ))
      */
  }
}
