package Micro.Actions.Combat.Decisionmaking

import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.{Sneak, Support}
import Micro.Actions.Combat.Spells.{SpiderMine, Stim}
import Micro.Actions.Combat.Tactics._
import Micro.Actions.Protoss._
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Fight extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    || unit.unitClass.isSiegeTank
    || unit.readyForAttackOrder
  )
  
  override def perform(unit: FriendlyUnitInfo) {
    Follow.consider(unit)
    Support.consider(unit)
    StrategicNuke.consider(unit)
    Cast.consider(unit)
    Detect.consider(unit)
    Stim.consider(unit)
    BeFlier.consider(unit)
    Paradrop.consider(unit)
    EmergencyBunk.consider(unit)
    Root.consider(unit)
    BeACarrier.consider(unit)
    BeAnArbiter.consider(unit)
    BeAReaver.consider(unit)
    Recharge.consider(unit)
    SpiderMine.consider(unit)
    Bust.consider(unit)
    Spot.consider(unit)
    Sneak.consider(unit)
    DefaultCombat.consider(unit)
    OccupyBunker.consider(unit)
  }
}
