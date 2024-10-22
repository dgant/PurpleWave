package Micro.Actions.Scouting

import Mathematics.Maff
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Agency.Commander
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object SabotageProxy extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.agent.isScout
    && unit.canAttackGround
    && unit.matchups.targets.exists(_.proxied))

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    unit.agent.toAttack = Maff.minBy(unit.matchups.targets.view.filter(u => u.unitClass.isBuilding || (u.unitClass.isWorker && u.visible)))(_.unitClass.maxTotalHealth)
    if (unit.matchups.threats.forall(threat =>
      threat.framesToGetInRange(unit) > 12
      || (threat.unitClass.isWorker
        && (threat.totalHealth <= unit.totalHealth
          || unit.agent.toAttack.exists(b =>
            Math.max(15 + threat.pixelRangeAgainst(unit), b.pixelDistanceEdge(unit)) < threat.pixelsToGetInRange(unit)))))) {
      Commander.attack(unit)
    }
    Retreat.delegate(unit)
  }
}
