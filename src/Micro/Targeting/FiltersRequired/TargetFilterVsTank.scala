package Micro.Targeting.FiltersRequired

import Lifecycle.With
import Micro.Targeting.TargetFilter
import Utilities.UnitFilters.IsTank
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, Orders, UnitInfo}

object TargetFilterVsTank extends TargetFilter {

  override def appliesTo(actor: FriendlyUnitInfo): Boolean = ! actor.flying

  override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    if ( ! target.player.isTerran) return true
    if (target.unitClass.suicides) return true
    if (Protoss.Reaver(actor) && actor.matchups.inTankRange) return true // A Reaver under pressure just needs to get shots off

    lazy val firingPixel = actor.pixelToFireAt(target)

    // Avoid eating tank fire while attacking unimportant units
    (target.isAny(IsTank, Terran.Battlecruiser, Terran.SpiderMine)
      // Do target tank repairers
      || target.order == Orders.Repair && target.orderTarget.exists(_.isAny(IsTank, Terran.Battlecruiser))
      // Do target anti-air when we have Carriers
      || (target.canAttackAir && actor.matchups.groupOf.count(Protoss.Carrier) > 0)
      // Do target anything that's in the way
      || (actor.readyForAttackOrder && ! target.flying && actor.pixelDistanceEdge(target) < 12)
      // If we're cloaked it doesn't matter
      || (actor.effectivelyCloaked && ! firingPixel.tile.enemyDetected)
      // Do target things we can hit without eating tank fire
      || ! With.grids.enemyRangeGround.unitsOn(firingPixel.tile).exists(t =>
        IsTank(t)
        && t.inRangeToAttack(actor, firingPixel.project(t.pixel, 16))))
  }
}
