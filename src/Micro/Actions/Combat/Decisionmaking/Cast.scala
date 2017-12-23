package Micro.Actions.Combat.Decisionmaking

import Micro.Actions.Action
import Micro.Actions.Combat.Spells._
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Cast extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.energy > 0
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    unit.agent.canCast = spells.exists(_.allowed(unit))
    spells.foreach(_.consider(unit))
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
