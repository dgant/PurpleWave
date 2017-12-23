package Micro.Actions.Combat.Decisionmaking

import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.{Cower, Sneak}
import Micro.Actions.Combat.Spells.{SpiderMine, Stim}
import Micro.Actions.Combat.Tactics._
import Micro.Actions.Protoss.{BeACarrier, BeACorsair, BeAnArbiter}
import Micro.Actions.Terran.{Siege, Unsiege}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Fight extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove || unit.unitClass.isSiegeTank || unit.readyForAttackOrder
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    StrategicNuke.consider(unit)
    Cast.consider(unit)
    Stim.consider(unit)
    Bunk.consider(unit)
    Cower.consider(unit)
    Siege.consider(unit)
    Unsiege.consider(unit)
    BeACarrier.consider(unit)
    BeAnArbiter.consider(unit)
    BeACorsair.consider(unit)
    Recover.consider(unit)
    SpiderMine.consider(unit)
    BustBunker.consider(unit)
    Spot.consider(unit)
    Sneak.consider(unit)
    if (unit.agent.shouldEngage) {
      Engage.consider(unit)
    }
    else {
      Disengage.consider(unit)
    }
    Form.consider(unit)
  }
}
