package Micro.Actions.Combat.Decisionmaking

import Micro.Actions.Action
import Micro.Actions.Combat.Attacking.{Potshot, Target}
import Micro.Actions.Combat.Maneuvering.AttackAndReposition
import Micro.Actions.Combat.Tactics.BustWallin
import Micro.Actions.Commands.Attack
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Engage extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.canFight &&
    unit.matchups.targets.nonEmpty
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    //Punch.consider(unit)
    Potshot.consider(unit)
    BustWallin.consider(unit)
    Target.delegate(unit)
    AttackAndReposition.consider(unit)
    Attack.consider(unit)
  }
}
