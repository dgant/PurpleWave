package Micro.Actions.Scouting

import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Scout extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.action.toTravel.isDefined &&
    unit.canAttackThisSecond
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    
  }
}
