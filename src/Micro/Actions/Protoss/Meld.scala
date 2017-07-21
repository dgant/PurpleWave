package Micro.Actions.Protoss

import Lifecycle.With
import Micro.Actions.Action
import Micro.Execution.ActionState
import ProxyBwapi.Races.Protoss

object Meld extends Action {
  override protected def allowed(state: ActionState): Boolean = {
    state.canMeld
  }
  
  override protected def perform(state: ActionState) {
    val besties = state.unit.matchups.allies.filter(u => u.actionState.canMeld)
    if (besties.nonEmpty) {
      val bestBestie = besties.minBy(_.pixelDistanceFast(state.unit))
      With.commander.useTechOnUnit(state.unit, Protoss.ArchonMeld, bestBestie)
    }
  }
}
