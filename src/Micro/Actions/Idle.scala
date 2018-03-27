package Micro.Actions

import Micro.Actions.Basic._
import Micro.Actions.Combat.Decisionmaking.{Fight, FightOrFlight}
import Micro.Actions.Combat.Maneuvering.{Dodge, Yank}
import Micro.Actions.Combat.Tactics.{Detect, Tickle, Unbunk}
import Micro.Actions.Commands.{Attack, Move}
import Micro.Actions.Protoss.Meld
import Micro.Actions.Scouting.Scout
import Micro.Actions.Transportation.Transport
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Idle extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    true
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    actions.foreach(_.consider(unit))
  }
  
  private val actions = Vector(
    Liftoff,
    Cancel,
    Unbunk,
    Meld,
    Build,
    Finish,
    FightOrFlight,
    Yank,
    Dodge,
    Tickle,
    EmergencyRepair,
    Gather,
    Addon,
    Scan,
    Unstick,
    Produce,
    Rally,
    ReloadInterceptors,
    ReloadScarabs,
    Pardon,
    Detect,
    Transport,
    Scout,
    Fight,
    Attack,
    Move
  )
}
