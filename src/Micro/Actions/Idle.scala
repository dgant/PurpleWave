package Micro.Actions

import Micro.Actions.Basic._
import Micro.Actions.Commands.{Attack, Travel}
import Micro.Task.ExecutionState

object Idle extends Action {
  
  override def allowed(state:ExecutionState):Boolean = {
    ! state.unit.attackStarting
  }
  
  def perform(state:ExecutionState) {
    
    state.toTravel    = state.intent.toTravel
    state.toAttack    = state.intent.toAttack
    state.toGather    = state.intent.toGather
    state.toBuild     = state.intent.toBuild
    state.toBuildTile = state.intent.toBuildTile
    state.toTrain     = state.intent.toTrain
    state.toTech      = state.intent.toTech
    state.toUpgrade   = state.intent.toUpgrade
    state.canAttack   = state.intent.canAttack
    state.canPursue   = state.intent.canPursue
    
    actions.foreach(_.consider(state))
  }
  
  private val actions = Vector(
    Gather,
    Build,
    Produce,
    ReloadInterceptors,
    ReloadScarabs,
    Fight,
    Attack,
    Travel
  )
}
