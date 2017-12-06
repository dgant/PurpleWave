package Micro.Actions.Scouting

import Micro.Actions.Action
import Micro.Actions.Combat.Tactics.Potshot
import Micro.Actions.Commands.Move
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Scout extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.toTravel.isDefined  &&
    unit.agent.canScout            &&
    unit.canAttack
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    if (
      unit.matchups.threats.forall(_.unitClass.isWorker) &&
      (unit.totalHealth >= unit.unitClass.maxTotalHealth || unit.matchups.framesToLiveDiffused > 24)) {
      Potshot.consider(unit)
    }
    PreserveScout.consider(unit)
    DisruptBuilder.consider(unit)
    BlockConstruction.consider(unit)
    FindBuildings.consider(unit)
    Poke.consider(unit)
    Move.consider(unit)
  }
}
