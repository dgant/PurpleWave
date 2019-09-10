package Micro.Actions.Scouting

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Techniques.Avoid
import Micro.Actions.Commands.{Attack, Move}
import Planning.UnitMatchers.UnitMatchWarriors
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, Orders, UnitInfo}

object BlockConstruction extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    blockableBuilders(unit).nonEmpty
      && ! unit.flying
      && ! unit.matchups.threats.exists(_.is(UnitMatchWarriors))
      && With.geography.enemyBases.nonEmpty
      && (unit.hitPoints > 10 || ! unit.base.exists(_.owner.isEnemy))
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    val builder = blockableBuilders(unit).minBy(_.pixelDistanceEdge(unit))
    val destination = builder.targetPixel.getOrElse(builder.pixelCenter)
    unit.agent.toAttack = Some(builder)
    
    if (unit.framesToGetInRange(builder) > With.reaction.agencyAverage) {
      unit.agent.toTravel = Some(unit.pixelCenter.project(builder.pixelCenter, unit.pixelDistanceCenter(builder) + 48))
      Move.delegate(unit)
    }
    else if (unit.readyForAttackOrder
      || unit.totalHealth > builder.totalHealth
      || unit.pixelDistanceEdge(builder) > 8) {
      Attack.delegate(unit)
    }
    else {
      Avoid.delegate(unit)
    }
  }
  
  val buildOrders = Vector(
    Orders.PlaceBuilding,
    Orders.ZergBuildingMorph,
    Orders.ConstructingBuilding,
    Orders.CreateProtossBuilding,
    Orders.PlaceProtossBuilding
  )
  
  def blockableBuilders(unit: FriendlyUnitInfo): Iterable[UnitInfo] = {
    unit.matchups.targets.filter(builder => {
      lazy val hasBuildOrder        = buildOrders.contains(builder.order)
      lazy val hasMoveOrder         = builder.order == Orders.Move
      lazy val hasRelevantOrder     = hasBuildOrder  || hasMoveOrder
      lazy val targetPixel          = builder.orderTargetPixel.orElse(builder.targetPixel).getOrElse(builder.pixelCenter)
      lazy val movingToRelevantBase = (
        hasRelevantOrder
        && targetPixel.base.exists(base =>
          (base.owner.isEnemy
          || (base.owner.isNeutral && ! base.isStartLocation && (unit.player.isZerg || base.isNaturalOf.exists(_.owner.isEnemy))))))
      val output                    = builder.unitClass.isWorker && (hasBuildOrder || movingToRelevantBase)
      output
    })
  }
}
