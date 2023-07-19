package Micro.Targeting.FiltersRequired

import Micro.Targeting.TargetFilter
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.UnitFilters.IsTank

object TargetFilterAntiAir extends TargetFilter {
  override def appliesTo(actor: FriendlyUnitInfo): Boolean = actor.flying

  // Avoid eating static anti-air shots unless necessary
  override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    var output  = false
    output ||= target.canAttack(actor)
    output ||= target.unitClass.canAttack(actor)
    output ||= target.isAny(IsTank, Protoss.Reaver, Protoss.HighTemplar, Zerg.Defiler)
    output ||= Stream(target.pixel, actor.pixelToFireAtSimple(target)).forall(_.tile.enemiesAttackingAir.forall( ! _.isAny(Terran.MissileTurret, Zerg.SporeColony)))
    output
  }
}
