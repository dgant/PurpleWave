package Micro.Actions.Combat.Decisionmaking

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Micro.Actions.Action
import Micro.Actions.Combat.Spells._
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Cast extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.energy > 0
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    unit.agent.canCast = spells.exists(_.allowed(unit))
    
    if (unit.agent.canCast) {
      spells.foreach(_.consider(unit))
    }
    if (unit.readyForMicro
      && unit.matchups.threats.nonEmpty
      && unit.isAny(
        Terran.ScienceVessel,
        Protoss.Arbiter,
        Protoss.DarkArchon,
        Protoss.HighTemplar,
        Zerg.Defiler,
        Zerg.Queen)
      && unit.matchups.framesOfSafety < GameTime(0, 5)()) {
      unit.agent.shouldEngage = false
    }
  }
  
  val spells = Array(
    Heal,
    WraithCloak,
    WraithUncloak,
    Yamato,
    Irradiate,
    DefensiveMatrix,
    PsionicStorm,
    Stasis,
    DisruptionWeb,
    MindControl,
    Feedback,
    GhostCloak,
    TacticalNuke,
    GhostUncloak,
    Lockdown
  )
}
