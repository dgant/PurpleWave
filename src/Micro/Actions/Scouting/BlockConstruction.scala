package Micro.Actions.Scouting

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Techniques.Avoid
import Micro.Actions.Commands.{Attack, Move}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import bwapi.UnitCommandType

object BlockConstruction extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    blockableBuilders(unit).nonEmpty && ! unit.flying
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    val builder = blockableBuilders(unit).minBy(_.pixelDistanceEdge(unit))
    val destination = builder.targetPixel
    
    var fight = false
    var block = false
    if (destination.isDefined) {
      if (unit.pixelDistanceCenter(destination.get) < builder.pixelDistanceCenter(destination.get)) {
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
      if (unit.pixelDistanceEdge(builder) >= 16 || unit.readyForAttackOrder) {
        Attack.delegate(unit)
      } else {
        Avoid.delegate(unit)
      }
    }
    else if (block) {
      unit.agent.toTravel = destination
      Move.delegate(unit)
    }
  }
  
  def blockableBuilders(unit: FriendlyUnitInfo): Iterable[UnitInfo] = {
    lazy val enemyBase = With.geography.enemyBases.headOption
      .getOrElse(With.geography.startBases.minBy(_.lastScoutedFrame))
    
    unit.matchups.targets.filter(builder =>
      builder.unitClass.isWorker &&
      (
        builder.command.exists(_.getUnitCommandType.toString == UnitCommandType.Build.toString)
        || builder.targetPixel.exists(targetPixel =>
          builder.pixelDistanceCenter(targetPixel) < 32.0 * 60.0 &&
          targetPixel.zone.bases.exists(base =>
            base.townHall.isEmpty &&
            base.townHallArea.contains(targetPixel.tileIncluding)))
        || builder.base.exists(base =>
          base.heart.groundPixels(With.geography.home) <
          base.heart.groundPixels(enemyBase.heart.pixelCenter)
          && builder.pixelDistanceCenter(base.heart.pixelCenter) <
          unit.pixelDistanceCenter(base.heart.pixelCenter) + 64)
      ))
  }
}
