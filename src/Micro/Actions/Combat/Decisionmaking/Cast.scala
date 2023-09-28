package Micro.Actions.Combat.Decisionmaking

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Spells._
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Cast extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.energyMax > 0
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    if (unit.cooldownSpell < With.latency.remainingFrames) {
      spells.foreach(_(unit))
    }
    if (unit.ready && unit.isAny(
      Terran.ScienceVessel,
      Protoss.Arbiter,
      Protoss.DarkArchon,
      Protoss.HighTemplar,
      Zerg.Defiler,
      Zerg.Queen)) {
      unit.agent.shouldFight &&= unit.unitClass.spells.exists(s => s() && unit.energy >= s.energyCost - 2)
    }
  }
  
  val spells: Array[Action] = Array(
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
