package Micro.Actions.Scouting

import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Scout extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.agent.isScout
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    SabotageProxy(unit)
    KnockKnock(unit)
    PreserveScout(unit)
    AttackBuilder(unit)
    BlockConstruction(unit)
    Search(unit)
    SearchWhenBored(unit)
    Commander.move(unit)
  }
}
