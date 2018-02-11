package Micro.Actions.Scouting

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Decisionmaking.Engage
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Poke extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    With.intelligence.enemyMain.isDefined
    && unit.canAttack
    && unit.matchups.targets.exists(_.unitClass.isWorker)
    && unit.matchups.threats.forall(_.unitClass.isWorker)
    && unit.totalHealth > Math.min(30, 10 * unit.matchups.framesOfEntanglementPerThreatDiffused.count(pair => ! pair._1.constructing && pair._2 > -12))
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    Engage.delegate(unit)
  }
}
