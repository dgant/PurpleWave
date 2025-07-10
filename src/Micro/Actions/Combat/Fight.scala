package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Actions.Combat.Decisionmaking.{Cast, Follow, Root}
import Micro.Actions.Combat.Maneuvering.Stealth
import Micro.Actions.Combat.Spells.Stim
import Micro.Actions.Combat.Tactics._
import Micro.Actions.Protoss._
import Micro.Actions.Terran.{BeCombatSCV, BeVulture}
import Micro.Actions.Zerg.BeMutalisk
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Fight extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = (unit.canAttack || unit.canMove) && ! unit.agent.isScout

  override def perform(unit: FriendlyUnitInfo): Unit = {
    Follow(unit)
    StrategicNuke(unit)
    Cast(unit)
    Spot(unit)
    Stim(unit)
    EmergencyBunk(unit)
    Root(unit)
    BeFlier(unit)
    BeCombatSCV(unit)
    BeVulture(unit)
    BeCarrier(unit)
    BeArbiter(unit)
    BeReaver(unit)
    BeMutalisk(unit)
    Recharge(unit)
    Bust(unit)
    Stealth(unit)
    unit.agent.combat.perform(unit)
    OccupyBunker(unit)
  }
}
