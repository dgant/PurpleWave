package Micro.Actions.Scouting

import Mathematics.Maff
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Agency.Commander
import Planning.UnitMatchers.MatchProxied
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Strategery.Strategies.Zerg.ZvE4Pool

object Sabotage extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.agent.isScout
    && unit.unitClass.isWorker
    && unit.matchups.targets.exists(MatchProxied)
    && ! ZvE4Pool.activate)

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val buildingTarget = Maff.minBy(unit.matchups.targets.view.filter(u => u.unitClass.isBuilding || (u.unitClass.isWorker && u.visible)))(_.unitClass.maxTotalHealth)
    if (unit.matchups.threats.forall(threat =>
      threat.framesToGetInRange(unit) > 12
      || (threat.unitClass.isWorker
        && (threat.totalHealth <= unit.totalHealth
          || buildingTarget.exists(b => Math.max(15 + threat.pixelRangeAgainst(unit), b.pixelDistanceEdge(unit)) < threat.pixelDistanceEdge(unit)))))) {
      unit.agent.toAttack = buildingTarget
      Commander.attack(unit)
    }
    Retreat.delegate(unit)
  }
}
