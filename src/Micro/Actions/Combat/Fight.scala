package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Actions.Combat.Decisionmaking.{Cast, Follow, Root}
import Micro.Actions.Combat.Maneuvering.Stealth
import Micro.Actions.Combat.Spells.{BeVulture, Stim}
import Micro.Actions.Combat.Tactics._
import Micro.Actions.Protoss._
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Fight extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = (unit.canAttack || unit.canMove) && ! unit.agent.isScout

  override def perform(unit: FriendlyUnitInfo): Unit = {
    Follow.apply(unit)
    StrategicNuke.apply(unit)
    Cast.apply(unit)
    Detect.apply(unit)
    Stim.apply(unit)
    EmergencyBunk.apply(unit)
    Root.apply(unit)
    BeFlier.apply(unit)
    BeCarrier.apply(unit)
    BeArbiter.apply(unit)
    BeReaver.apply(unit)
    BeVulture.apply(unit)
    Recharge.apply(unit)
    Bust.apply(unit)
    Spot.apply(unit)
    Stealth.apply(unit)
    unit.agent.combat.perform(unit)
    OccupyBunker.apply(unit)
  }
}
