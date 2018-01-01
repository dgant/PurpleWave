package Micro.Actions.Combat.Techniques.Common

import Micro.Actions.Action
import Micro.Actions.Combat.Techniques.{Avoid, Retreat}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Leave extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    Weigh.consider(unit, Avoid, Retreat)
  }
}
