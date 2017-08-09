package Micro.Actions.Combat.Decisionmaking

import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.{Cower, Sneak}
import Micro.Actions.Combat.Spells.{SpiderMine, Stim}
import Micro.Actions.Combat.Tactics.{Bunk, BustBunker, Sally, Spot}
import Micro.Actions.Protoss.BeACarrier
import Micro.Actions.Terran.{Siege, Unsiege}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Fight extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove || unit.unitClass.isSiegeTank || unit.readyForAttackOrder
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    Stim.consider(unit)
    Cast.consider(unit)
    Bunk.consider(unit)
    Cower.consider(unit)
    Siege.consider(unit)
    Unsiege.consider(unit)
    BeACarrier.consider(unit)
    Recover.consider(unit)
    SpiderMine.consider(unit)
    BustBunker.consider(unit)
    Sally.consider(unit)
    Spot.consider(unit)
    Sneak.consider(unit)
    if (unit.agent.shouldEngage) {
      Engage.consider(unit)
    }
    else {
      Disengage.consider(unit)
    }
  }
}
