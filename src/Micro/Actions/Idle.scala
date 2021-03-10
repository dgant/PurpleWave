package Micro.Actions

import Micro.Actions.Basic._
import Micro.Actions.Combat.Decisionmaking.{Fight, FightOrFlight}
import Micro.Actions.Combat.Tactics.{Tickle, Unbunk}
import Micro.Actions.Commands.{Attack, Move}
import Micro.Actions.Protoss.Meld
import Micro.Actions.Scouting.Scout
import Micro.Actions.Transportation.Transport
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Idle extends Action {

  @inline
  final override def allowed(unit: FriendlyUnitInfo): Boolean = true

  @inline
  final override def perform(unit: FriendlyUnitInfo): Unit = actions.foreach(_.consider(unit))
  
  private val actions = Vector(
    Liftoff,
    Cancel,
    Unbunk,
    Meld,
    Build,
    Finish,
    FightOrFlight,
    Tickle,
    Repair,
    EmergencyRepair,
    Gather,
    Addon,
    Scan,
    Unstick,
    Produce,
    Rally,
    ReloadInterceptors,
    ReloadScarabs,
    Transport,
    Scout,
    Bunk,
    Fight,
    Attack,
    Move
  )
}
