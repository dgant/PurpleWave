package Micro.Actions.Scouting

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Agency.Commander
import ProxyBwapi.Orders
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.?
import Utilities.UnitFilters.{IsWarrior, IsWorker}

object BlockConstruction extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    blockableBuilders(unit).nonEmpty
      && ! unit.flying
      && ! unit.matchups.threats.exists(IsWarrior)
      && With.geography.enemyBases.nonEmpty)
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val builder         = blockableBuilders(unit).minBy(_.pixelDistanceEdge(unit))
    val builderTarget   = builder.orderTargetPixel.orElse(builder.targetPixel).getOrElse(builder.pixel)
    val destination     = builderTarget.base
      .filter(b => b.naturalOf.isDefined && b.townHall.isEmpty)
      .map(_.townHallArea.center)
      .getOrElse(unit.pixel.project(builder.pixel, unit.pixelDistanceCenter(builder) + 48))


    unit.agent.redoubt.set(destination)
    unit.agent.decision.set(destination)
    unit.agent.toAttack = Some(builder)

    if (unit.matchups.threatsInPixels(32).exists(builder !=)) {
      Retreat.delegate(unit)

    } else if (unit.framesToGetInRange(builder) > With.reaction.agencyAverage) {
      Commander.move(unit)

    } else if (unit.readyForAttackOrder
      || unit.totalHealth >= builder.totalHealth + ?(unit.cooldownLeft == builder.cooldownLeft, 0, ?(unit.cooldownLeft < builder.cooldownLeft, 5, -5))
      || unit.pixelsToGetInRange(builder) > 0) {

      Commander.attack(unit)

    } else {
      Retreat.delegate(unit)
    }
  }
  
  val constructionOrders: Seq[String] = Vector(
    Orders.PlaceBuilding,
    Orders.ZergBuildingMorph,
    Orders.CreateProtossBuilding,
    Orders.PlaceProtossBuilding
  )

  def blockableBuilders(unit: FriendlyUnitInfo): Iterable[UnitInfo] = {
    unit.matchups.targets.filter(builder => {
      lazy val hasBuildOrder        = constructionOrders.contains(builder.order)
      lazy val hasMoveOrder         = builder.order == Orders.Move
      lazy val hasRelevantOrder     = hasBuildOrder  || hasMoveOrder
      lazy val targetPixel          = builder.orderTargetPixel.orElse(builder.targetPixel).getOrElse(builder.pixel)
      lazy val sittingOnExpansion   = builder.base.exists(b => b.townHall.isEmpty && b.naturalOf.forall( ! _.isOurs) && b.townHallArea.contains(builder.pixel))
      lazy val movingToRelevantBase = (
        hasRelevantOrder
        && targetPixel.base.exists(base =>
          base.townHall.isEmpty
          && (base.owner.isEnemy
            || (base.owner.isNeutral
              && ! base.isMain
              && (unit.player.isZerg || base.naturalOf.exists(_.owner.isEnemy))))))

      val output = IsWorker(builder) && (hasBuildOrder || movingToRelevantBase || sittingOnExpansion)
      output
    })
  }
}
