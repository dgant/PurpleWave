package Micro.Actions.Scouting

import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Scout extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.agent.toTravel.isDefined && unit.agent.isScout
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    SabotageProxy.consider(unit)
    KnockKnock.consider(unit)
    PreserveScout.consider(unit)
    AttackBuilder.consider(unit)
    BlockConstruction.consider(unit)
    Search.consider(unit)
    SearchWhenBored.consider(unit)
    Commander.move(unit)
  }
}
