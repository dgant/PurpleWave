package Micro.Actions.Combat.Targeting.Filters

import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterCarrierShootsUp extends TargetFilter {
  override def appliesTo(actor: FriendlyUnitInfo): Boolean = actor.isCarrier()
  override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    if ( ! actor.is(Protoss.Carrier)) return true

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
