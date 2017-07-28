package Micro.Actions

import Micro.Actions.Basic._
import Micro.Actions.Combat.Decisionmaking.Fight
import Micro.Actions.Combat.Maneuvering.Duck
import Micro.Actions.Combat.Tactics.Tickle
import Micro.Actions.Commands.{Attack, Travel}
import Micro.Actions.Protoss.Meld
import Micro.Actions.Scouting.Scout
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Idle extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    true
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    actions.foreach(_.consider(unit))
  }
  
  private val actions = Vector(
    Cancel, //Probably not actually used yet because candidates won't be in the Executor queue
    Meld,
    Tickle,
    Duck,
    Gather,
    Build,
    Unstick,
    Produce,
    Rally,
    ReloadInterceptors,
    ReloadScarabs,
    Pardon,
    Scout,
    Fight,
    Attack,
    Travel
  )
}
