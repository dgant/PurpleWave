package Micro.Actions.Scouting

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Techniques.Avoid
import Micro.Actions.Commands.{Attack, Move}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, Orders, UnitInfo}

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
    else if (unit.readyForAttackOrder
      || unit.totalHealth > builder.totalHealth
      || unit.pixelDistanceEdge(builder) > 8) {
      Attack.delegate(unit)
    }
    else {
      Avoid.delegate(unit)
    }
  }
  
  private val buildOrders = Vector(
    Orders.PlaceBuilding,
    Orders.ZergBuildingMorph,
    Orders.ConstructingBuilding,
    Orders.CreateProtossBuilding,
    Orders.PlaceProtossBuilding
  )
  
  def blockableBuilders(unit: FriendlyUnitInfo): Iterable[UnitInfo] = {
    lazy val enemyBase = With.geography.enemyBases.headOption
      .getOrElse(With.geography.startBases.minBy(_.lastScoutedFrame))
    
    unit.matchups.targets.filter(builder => {
      lazy val hasBuildOrder  = buildOrders.contains(builder.order)
      lazy val hasMoveOrder   = builder.order == Orders.Move
      lazy val targetPixel    = builder.orderTargetPixel.orElse(builder.targetPixel).getOrElse(builder.pixelCenter)
      lazy val movingToRelevantBase = hasBuildOrder && targetPixel.base.exists(base =>
        (base.owner.isEnemy || (base.owner.isNeutral && ! base.isStartLocation)))
      val output = builder.unitClass.isWorker && (hasBuildOrder || movingToRelevantBase)
      output
    })
  }
}
