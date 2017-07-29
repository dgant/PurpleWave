package Micro.Actions.Scouting

import Micro.Actions.Action
import Micro.Actions.Combat.Decisionmaking.Engage
import Micro.Actions.Commands.Travel
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import bwapi.UnitCommandType

object BlockConstruction extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    blockableBuilders(unit).nonEmpty
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    val builder = blockableBuilders(unit).minBy(_.pixelDistanceFast(unit))
    val destination = builder.targetPixel
    
    var fight = false
    var block = false
    if (destination.isDefined) {
      if (unit.pixelDistanceFast(destination.get) < builder.pixelDistanceFast(destination.get)) {
        fight = true
      }
      else {
        block = true
      }
    }
    else {
      fight = true
    }
    if (fight) {
      unit.agent.toAttack = Some(builder)
      Engage.delegate(unit)
    }
    else if (block) {
      unit.agent.toTravel = destination
      Travel.delegate(unit)
    }
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
