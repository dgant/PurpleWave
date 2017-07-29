package Micro.Actions.Scouting

import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Kite
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Poke extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.matchups.targets.exists(_.unitClass.isWorker)  &&
    unit.matchups.threatsViolent.isEmpty                &&
    ! unit.wounded
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    Kite.delegate(unit)
  }
}
