package Micro.Targeting.FiltersRequired

import Lifecycle.With
import Micro.Targeting.TargetFilter
import Utilities.UnitFilters.IsTank
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, Orders, UnitInfo}

object TargetFilterVsTank extends TargetFilter {
  override def appliesTo(actor: FriendlyUnitInfo): Boolean = ! actor.flying
  override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    if ( ! target.player.isTerran) return true
    if (target.unitClass.suicides) return true
    if (With.reaction.sluggishness > 0) return true
    lazy val firingPixel = actor.pixelToFireAt(target)
    (target.isAny(IsTank, Terran.Battlecruiser)
      || target.order == Orders.Repair && target.orderTarget.exists(_.isAny(IsTank, Terran.Battlecruiser))
      || ! With.grids.enemyRangeGround.unitsOn(firingPixel.tile).exists(t => t.inRangeToAttack(actor, firingPixel.project(t.pixel, 16))))
  }
}
