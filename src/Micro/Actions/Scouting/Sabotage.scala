package Micro.Actions.Scouting

import Micro.Actions.Action
import Micro.Actions.Combat.Techniques.Avoid
import Micro.Actions.Commands.Attack
import Planning.UnitMatchers.UnitMatchProxied
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

object Sabotage extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.agent.canScout
    && unit.matchups.targets.exists(_.is(UnitMatchProxied)))

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    if (unit.matchups.threats.forall(t => t.framesToGetInRange(unit) > 12 || (t.unitClass.isWorker && t.totalHealth <= unit.totalHealth))) {
      unit.agent.toAttack = ByOption.minBy(unit.matchups.targets.view.filter(u =>
        u.unitClass.isBuilding || (u.unitClass.isWorker && u.visible)))(_.unitClass.maxTotalHealth)
      Attack.delegate(unit)
    }

    Avoid.delegate(unit)
  }
}
