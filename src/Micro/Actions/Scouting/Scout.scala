package Micro.Actions.Scouting

import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Scout extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.agent.toTravel.isDefined && unit.agent.isScout
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    SabotageProxy.apply(unit)
    KnockKnock.apply(unit)
    PreserveScout.apply(unit)
    AttackBuilder.apply(unit)
    BlockConstruction.apply(unit)
    Search.apply(unit)
    SearchWhenBored.apply(unit)
    Commander.move(unit)
  }
}
