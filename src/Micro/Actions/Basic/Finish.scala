package Micro.Actions.Basic

import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Finish extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.toFinish.isDefined
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    Commander.rightClick(unit, unit.agent.toFinish.get)
  }
}
