package Micro.Actions.Combat.Maneuvering

import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Path extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove && unit.agent.toTravel.isDefined
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    unit.agent.toTravel = unit.agent.toTravel.map(unit.agent.nextWaypoint)
  }
}
