package Micro.Actions.Basic

import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Liftoff extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.intent.shouldLiftoff && ! unit.flying
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    Commander.lift(unit)
  }
}
