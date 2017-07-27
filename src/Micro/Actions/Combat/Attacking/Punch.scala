package Micro.Actions.Combat.Attacking

import Micro.Actions.Action
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Punch extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.action.canFight                  &&
    unit.canAttack              &&
    unit.melee                            &&
    unit.matchups.targetsInRange.nonEmpty &&
    {
      val nearestEnemy = unit.matchups.targetsInRange.minBy(_.pixelDistanceFast(unit))
      nearestEnemy.is(Zerg.Zergling)
    }
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    Potshot.delegate(unit)
  }
}
