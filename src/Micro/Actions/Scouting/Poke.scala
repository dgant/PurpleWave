package Micro.Actions.Scouting

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Techniques.Breathe
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Poke extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    With.intelligence.enemyMain.isDefined
    && unit.canAttack
    && unit.matchups.targets.exists(_.unitClass.isWorker)
    && unit.matchups.threats.forall(_.unitClass.isWorker)
    && unit.totalHealth > 10
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    Breathe.delegate(unit)
  }
}
