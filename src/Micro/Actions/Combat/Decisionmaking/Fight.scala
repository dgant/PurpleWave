package Micro.Actions.Combat.Decisionmaking

import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.{Cower, Sneak}
import Micro.Actions.Combat.Spells.SpiderMine
import Micro.Actions.Combat.Tactics.{BustBunker, Sally}
import Micro.Actions.Protoss.BeACarrier
import Micro.Actions.Terran.{Siege, Unsiege}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Fight extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove || unit.unitClass.isSiegeTank || unit.readyForAttackOrder
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    Cower.consider(unit)
    Siege.consider(unit)
    Unsiege.consider(unit)
    BeACarrier.consider(unit)
    Recover.consider(unit)
    SpiderMine.consider(unit)
    Cast.consider(unit)
    BustBunker.consider(unit)
    Sally.consider(unit)
    Sneak.consider(unit)
    if (unit.agent.shouldEngage) {
      Engage.consider(unit)
    }
    else {
      Disengage.consider(unit)
    }
  }
}
