package Micro.Actions.Combat.Decisionmaking

import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Avoid
import Micro.Actions.Combat.Spells.{PsionicStorm, SpiderMine, Stasis}
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Cast extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.energy > 0
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    SpiderMine.consider(unit)
    PsionicStorm.consider(unit)
    Stasis.consider(unit)
    if (unit.matchups.ifAt(framesAhead = 48).threatsInRange.nonEmpty && ( ! unit.canAttack || unit.is(Protoss.Arbiter))) {
      Avoid.consider(unit)
    }
  }
}
