package Micro.Actions.Scouting

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Techniques.Avoid
import Micro.Actions.Commands.{Attack, Move}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import bwapi.UnitCommandType

object BlockConstruction extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    blockableBuilders(unit).nonEmpty
      && ! unit.flying
      && With.geography.enemyBases.nonEmpty
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    val builder = blockableBuilders(unit).minBy(_.pixelDistanceEdge(unit))
    val destination = builder.targetPixel.getOrElse(builder.pixelCenter)
    unit.agent.toAttack = Some(builder)
    
    if (unit.framesToGetInRange(builder) > With.reaction.agencyAverage) {
      unit.agent.toTravel = Some(unit.pixelCenter.project(builder.pixelCenter,
        unit.pixelDistanceCenter(builder)
        + unit.unitClass.haltPixels
        + unit.topSpeed * With.reaction.agencyAverage))
      Move.delegate(unit)
    }
    else if (unit.readyForAttackOrder || unit.pixelDistanceEdge(builder) > 16) {
      Attack.delegate(unit)
    }
    else {
      Avoid.delegate(unit)
    }
  }
  
  def blockableBuilders(unit: FriendlyUnitInfo): Iterable[UnitInfo] = {
    lazy val enemyBase = With.geography.enemyBases.headOption
      .getOrElse(With.geography.startBases.minBy(_.lastScoutedFrame))
    
    unit.matchups.targets.filter(builder => {
      
      lazy val hasBuildOrder = builder.command.exists(_.getUnitCommandType.toString == UnitCommandType.Build.toString)
      lazy val targetPixel = builder.targetPixel.getOrElse(builder.pixelCenter)
      lazy val targetBase = targetPixel.base
      
      lazy val movingToTownHallArea = (
        builder.pixelDistanceCenter(targetPixel) < 32.0 * 60.0
        && targetBase.exists(_.townHall.isEmpty)
        && targetBase.exists(_.townHallArea.contains(targetPixel.tileIncluding)))
      
      lazy val movingToPossibleExpansion = targetBase.exists(base =>
        base.owner.isNeutral &&
          base.heart.groundPixels(enemyBase.heart.pixelCenter) <
          base.heart.groundPixels(With.geography.home))
      
      lazy val suspiciouslyIdle = targetBase.exists(base =>
        base.owner == builder.player
        && ! builder.gathering
        && ! builder.attacking)
      
      val output = builder.unitClass.isWorker && (
        movingToTownHallArea
        || movingToPossibleExpansion
        || suspiciouslyIdle)
      
      output
    })
  }
}
