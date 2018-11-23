package Micro.Actions.Protoss.Carrier

import Micro.Actions.Combat.Targeting.Filters.TargetFilter
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object CarrierTargetFilterShootsUp extends TargetFilter {
  override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    lazy val isRepairer = (
      target.is(Terran.SCV)
      && target.matchups.allies.exists(repairee =>
        repairee.unitClass.isMechanical
        && repairee.canAttack(actor)
        && target.pixelDistanceEdge(repairee) < 32))

    // Anything that can hit us or our interceptors
    val inRange = target.pixelDistanceEdge(actor) < target.pixelRangeAir + 32.0 * 8.0

    inRange && (target.canAttack(actor) || isRepairer)
  }
}
