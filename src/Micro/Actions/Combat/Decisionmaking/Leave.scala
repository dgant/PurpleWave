package Micro.Actions.Combat.Decisionmaking

import Micro.Actions.Action
import Micro.Actions.Combat.Techniques.Common.Weigh
import Micro.Actions.Combat.Techniques._
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Leave extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    unit.agent.toTravel = Some(unit.agent.origin)
    Weigh.consider(unit,
      Avoid,
      Retreat)
  }
}