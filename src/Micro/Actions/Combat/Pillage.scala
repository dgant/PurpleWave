package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Actions.Commands.Attack
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Pillage extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canAttackThisSecond  &&
    unit.matchups.threats.isEmpty           &&
    unit.matchups.targets.nonEmpty          &&
    (unit.action.toTravel.isEmpty || unit.pixelDistanceFast(unit.action.toTravel.get) < 32.0 * 8.0)
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    Target.delegate(unit)
    Attack.delegate(unit)
  }
}
