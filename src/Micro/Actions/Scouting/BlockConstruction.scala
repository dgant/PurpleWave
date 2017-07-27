package Micro.Actions.Scouting

import Micro.Actions.Action
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import bwapi.UnitCommandType

object BlockConstruction extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    blockableBuilders(unit).nonEmpty
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    
    
  }
  
  def blockableBuilders(unit: FriendlyUnitInfo): Iterable[UnitInfo] = {
    unit.matchups.targets.filter(builder =>
      builder.unitClass.isWorker &&
      (
        builder.command.exists(_.getUnitCommandType == UnitCommandType.Build) ||
        builder.targetPixel.exists(targetPixel => targetPixel.zone.bases.exists(_.townHallArea.contains(targetPixel.tileIncluding)))
      ))
  }
}
