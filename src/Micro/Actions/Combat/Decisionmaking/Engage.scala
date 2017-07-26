package Micro.Actions.Combat.Decisionmaking

import Micro.Actions.Action
import Micro.Actions.Combat.Attacking.{Punch, Target}
import Micro.Actions.Combat.Maneuvering.Kite
import Micro.Actions.Combat.Tactics.BustWallin
import Micro.Actions.Commands.Attack
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Engage extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.action.canFight &&
    unit.matchups.targets.nonEmpty
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    Punch.consider(unit)
    BustWallin.consider(unit)
    Target.delegate(unit)
    if ( ! unit.readyForAttackOrder) {
      Kite.delegate(unit)
    }
    Attack.delegate(unit)
  }
}
