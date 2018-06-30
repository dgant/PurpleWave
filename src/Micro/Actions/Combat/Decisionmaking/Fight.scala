package Micro.Actions.Combat.Decisionmaking

import Micro.Actions.Action
import Micro.Actions.Combat.Duck
import Micro.Actions.Combat.Maneuvering.{GooseChase, Sneak}
import Micro.Actions.Combat.Spells.{SpiderMine, Stim}
import Micro.Actions.Combat.Tactics.SpiderMines.HandleMines
import Micro.Actions.Combat.Tactics._
import Micro.Actions.Protoss.{BeACarrier, BeAnArbiter}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Fight extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    || unit.unitClass.isSiegeTank
    || unit.readyForAttackOrder
  )
  
  override def perform(unit: FriendlyUnitInfo) {
    GooseChase.consider(unit)
    StrategicNuke.consider(unit)
    Cast.consider(unit)
    Stim.consider(unit)
    HandleMines.consider(unit)
    Bunk.consider(unit)
    Root.consider(unit)
    BeACarrier.consider(unit)
    BeAnArbiter.consider(unit)
    Recharge.consider(unit)
    Reload.consider(unit)
    SpiderMine.consider(unit)
    Bust.consider(unit)
    Spot.consider(unit)
    Sneak.consider(unit)
    Duck.consider(unit)
    Unduck.consider(unit)
    if (unit.agent.shouldEngage) {
      Engage.consider(unit)
    }
    else {
      Disengage.consider(unit)
    }
    OccupyBunker.consider(unit)
  }
}
