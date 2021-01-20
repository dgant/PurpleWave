package Micro.Actions.Basic

import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Liftoff extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.agent.canLiftoff
    && unit.unitClass.isFlyingBuilding
    && ! unit.flying
  )
  
  override def perform(unit: FriendlyUnitInfo) {
    Commander.lift(unit)
  }
}
