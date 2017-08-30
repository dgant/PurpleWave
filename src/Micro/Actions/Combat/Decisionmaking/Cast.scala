package Micro.Actions.Combat.Decisionmaking

import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Avoid
import Micro.Actions.Combat.Spells._
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Cast extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.energy > 0
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    Heal.consider(unit)
    WraithCloak.consider(unit)
    WraithUncloak.consider(unit)
    Yamato.consider(unit)
    Irradiate.consider(unit)
    DefensiveMatrix.consider(unit)
    PsionicStorm.consider(unit)
    Stasis.consider(unit)
    MindControl.consider(unit)
    Feedback.consider(unit)
    if (unit.matchups.framesOfSafetyDiffused < 24 && ( ! unit.canAttack || unit.is(Protoss.Arbiter))) {
      Avoid.consider(unit)
    }
  }
}
