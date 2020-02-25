package Micro.Actions.Scouting

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Techniques.Avoid
import Micro.Actions.Commands.Attack
import Planning.UnitMatchers.UnitMatchProxied
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Strategery.Strategies.Zerg.ZvE4Pool
import Utilities.ByOption

object Sabotage extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.agent.canScout
    && unit.matchups.targets.exists(_.is(UnitMatchProxied))
    && ! With.strategy.selectedCurrently.contains(ZvE4Pool))

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val buildingTarget = ByOption.minBy(unit.matchups.targets.view.filter(u => u.unitClass.isBuilding || (u.unitClass.isWorker && u.visible)))(_.unitClass.maxTotalHealth)
    if (unit.matchups.threats.forall(threat =>
      threat.framesToGetInRange(unit) > 12
      || (threat.unitClass.isWorker
        && (threat.totalHealth <= unit.totalHealth
          || buildingTarget.exists(b => Math.max(15 + threat.pixelRangeAgainst(unit), b.pixelDistanceEdge(unit)) < threat.pixelDistanceEdge(unit)))))) {
      unit.agent.toAttack = buildingTarget
      Attack.delegate(unit)
    }

    Avoid.delegate(unit)
  }
}
