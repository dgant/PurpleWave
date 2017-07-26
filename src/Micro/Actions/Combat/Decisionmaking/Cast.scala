package Micro.Actions.Combat.Decisionmaking

import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.HoverOutsideRange
import Micro.Actions.Combat.Spells.PsionicStorm
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Cast extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.energy > 0
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    PsionicStorm.consider(unit)
    HoverOutsideRange.consider(unit)
  }
}
