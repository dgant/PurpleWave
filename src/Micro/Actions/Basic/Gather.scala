package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Attacking.Potshot
import Micro.Actions.Combat.Decisionmaking.Fight
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Gather extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.toGather.isDefined
  }
  
  override def perform(unit: FriendlyUnitInfo) {
  
    Potshot.consider(unit)
    
    if ( ! unit.agent.toGather.exists(_.pixelCenter.zone == unit.pixelCenter.zone)) {
      if (unit.matchups.threats.exists(_.framesBeforeAttacking(unit) < 10)) {
        Fight.consider(unit)
      }
    }
    
    With.commander.gather(unit, unit.agent.toGather.get)
  }
}
