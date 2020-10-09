package Micro.Actions.Combat.Decisionmaking

import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Engage extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.canFight &&
    unit.matchups.targets.nonEmpty
  }

  override def perform(unit: FriendlyUnitInfo): Unit = {
    EngageDisengage.NewEngage.consider(unit)
  }
}
