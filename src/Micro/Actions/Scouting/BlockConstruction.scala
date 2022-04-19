package Micro.Actions.Scouting

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Agency.Commander
import Utilities.UnitFilters.IsWarrior
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, Orders, UnitInfo}

object BlockConstruction extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    blockableBuilders(unit).nonEmpty
      && ! unit.flying
      && ! unit.matchups.threats.exists(IsWarrior)
      && With.geography.enemyBases.nonEmpty
      && (unit.hitPoints > 10 || ! unit.base.exists(_.owner.isEnemy))
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    val builder = blockableBuilders(unit).minBy(_.pixelDistanceEdge(unit))
    val destination = builder.targetPixel.getOrElse(builder.pixel)
    unit.agent.toAttack = Some(builder)
    
    if (unit.framesToGetInRange(builder) > With.reaction.agencyAverage) {
      unit.agent.toTravel = Some(unit.pixel.project(builder.pixel, unit.pixelDistanceCenter(builder) + 48))
      Commander.move(unit)
    }
    else if (unit.readyForAttackOrder
      || unit.totalHealth > builder.totalHealth
      || unit.pixelDistanceEdge(builder) > 8) {
      Commander.attack(unit)
    } else {
      Retreat.delegate(unit)
    }
  }
  
  val constructionOrders = Vector(
    Orders.PlaceBuilding,
    Orders.ZergBuildingMorph,
    Orders.ConstructingBuilding,
    Orders.CreateProtossBuilding,
    Orders.PlaceProtossBuilding
  )

  def blockableBuilders(unit: FriendlyUnitInfo): Iterable[UnitInfo] = {
    unit.matchups.targets.filter(builder => {
      lazy val hasBuildOrder        = constructionOrders.contains(builder.order)
      lazy val hasMoveOrder         = builder.order == Orders.Move
      lazy val hasRelevantOrder     = hasBuildOrder  || hasMoveOrder
      lazy val targetPixel          = builder.orderTargetPixel.orElse(builder.targetPixel).getOrElse(builder.pixel)
      lazy val movingToRelevantBase = (
        hasRelevantOrder
        && targetPixel.base.exists(base =>
          base.townHall.isEmpty
          && (base.owner.isEnemy
            || (base.owner.isNeutral && ! base.isStartLocation && (unit.player.isZerg || base.naturalOf.exists(_.owner.isEnemy))))))
      val output                    = builder.unitClass.isWorker && (hasBuildOrder || movingToRelevantBase)
      output
    })
  }
}
