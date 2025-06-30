package Micro.Actions

import Micro.Actions.Basic._
import Micro.Actions.Combat.Decisionmaking.FightOrFlee
import Micro.Actions.Combat.Fight
import Micro.Actions.Combat.Tactics.Unbunk
import Micro.Actions.Commands.{Attack, Travel}
import Micro.Actions.Protoss.{Meld, ReloadInterceptors, ReloadScarabs}
import Micro.Actions.Scouting.Scout
import Micro.Actions.Terran.{GetRepairedBuilding, FinishConstruction, GetRepairedMobile, Liftoff, Repair, Scan}
import Micro.Actions.Transportation.Transport
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Idle extends Action {

  @inline
  final override def allowed(unit: FriendlyUnitInfo): Boolean = true

  @inline
  final override def perform(unit: FriendlyUnitInfo): Unit = actions.foreach(_(unit))

  private val actions = Vector(
    Cancel,
    Liftoff,
    Unbunk,
    Meld,
    Build,
    FinishConstruction,
    FightOrFlee,
    Repair,
    GetRepairedMobile,
    GetRepairedBuilding,
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
    Crack,
    Fight,
    Attack,
    Travel
  )
}
