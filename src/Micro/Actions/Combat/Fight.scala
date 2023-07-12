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
    Follow(unit)
    StrategicNuke(unit)
    Cast(unit)
    SpotNew(unit)
    Stim(unit)
    EmergencyBunk(unit)
    Root(unit)
    BeFlier(unit)
    BeCarrier(unit)
    BeArbiter(unit)
    BeReaver(unit)
    BeVulture(unit)
    Recharge(unit)
    Bust(unit)
    Stealth(unit)
    unit.agent.combat.perform(unit)
    OccupyBunker(unit)
  }
}
