package Micro.Actions.Scouting

import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object KnockKnock extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.agent.isScout
    && (unit.zone != unit.agent.destinationFinal().zone || unit.agent.destinationFinal().zone.edges.exists(_.contains(unit.pixel)))
    && unit.matchups.threats.size == 1
    && unit.matchups.threats.forall(threat =>
      unit.canAttack(threat)
      && threat.unitClass.isWorker
      && unit.agent.destinationFinal().zone.edges.exists(_.contains(threat.pixel))
      && unit.totalHealth >= Math.min(11, threat.totalHealth)))

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    unit.agent.toAttack = unit.matchups.threats.headOption
    Commander.attack(unit)
  }
}
