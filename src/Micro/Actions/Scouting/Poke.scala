package Micro.Actions.Scouting

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.OldAttackAndReposition
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Poke extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    With.intelligence.enemyMain.isDefined               &&
    unit.matchups.targets.exists(_.unitClass.isWorker)  &&
    unit.matchups.threats.isEmpty                       &&
    unit.totalHealth > 10
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    OldAttackAndReposition.delegate(unit)
  }
}
