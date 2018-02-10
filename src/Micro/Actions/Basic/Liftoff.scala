package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Liftoff extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.agent.canLiftoff
    && unit.unitClass.isFlyingBuilding
    && ! unit.flying
  )
  
  override def perform(unit: FriendlyUnitInfo) {
    With.commander.lift(unit)
  }
}
