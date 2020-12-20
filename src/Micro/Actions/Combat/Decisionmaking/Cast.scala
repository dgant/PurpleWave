package Micro.Actions.Combat.Decisionmaking

import Micro.Actions.Action
import Micro.Actions.Combat.Spells._
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Cast extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.energy > 0
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    spells.foreach(_.consider(unit))
    if (unit.ready && unit.isAny(
      Terran.ScienceVessel,
      Protoss.Arbiter,
      Protoss.DarkArchon,
      Protoss.HighTemplar,
      Zerg.Defiler,
      Zerg.Queen)) {
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
