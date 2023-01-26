package Micro.Actions.Basic

import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object FinishConstruction extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.intent.toFinish.isDefined
  }

  override def perform(unit: FriendlyUnitInfo) {
    Commander.rightClick(unit, unit.intent.toFinish.get)
  }
}
