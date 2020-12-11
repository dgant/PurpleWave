package Micro.Actions.Combat.Decisionmaking

import Micro.Actions.Action
import Micro.Actions.Combat.Spells._
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.Seconds

object Cast extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.energy > 0
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    unit.agent.canCast = spells.exists(_.allowed(unit))
    
    if (unit.agent.canCast) {
      spells.foreach(_.consider(unit))
    }
    if (unit.ready
      && unit.matchups.threats.nonEmpty
      && unit.isAny(
        Terran.ScienceVessel,
        Protoss.Arbiter,
        Protoss.DarkArchon,
        Protoss.HighTemplar,
        Zerg.Defiler,
        Zerg.Queen)
      && unit.matchups.framesOfSafety < Seconds(5)()) {
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
