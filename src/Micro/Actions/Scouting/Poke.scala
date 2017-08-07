package Micro.Actions.Scouting

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.AttackAndReposition
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Poke extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    With.intelligence.enemyMain.isDefined               &&
    unit.matchups.targets.exists(_.unitClass.isWorker)  &&
    unit.matchups.threatsViolent.isEmpty                &&
    ! unit.wounded
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    AttackAndReposition.delegate(unit)
  }
}
