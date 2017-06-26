package Micro.Actions

import Micro.Actions.Basic._
import Micro.Actions.Combat.{Duck, Fight}
import Micro.Actions.Commands.{Attack, Travel}
import Micro.Execution.ActionState

object Idle extends Action {
  
  override def allowed(state: ActionState): Boolean = {
    ! state.unit.attackStarting
  }
  
  override def perform(state: ActionState) {
    
    state.toReturn        = state.intent.toReturn
    state.toTravel        = state.intent.toTravel
    state.toAttack        = state.intent.toAttack
    state.toGather        = state.intent.toGather
    state.toBuild         = state.intent.toBuild
    state.toBuildTile     = state.intent.toBuildTile
    state.toTrain         = state.intent.toTrain
    state.toTech          = state.intent.toTech
    state.toUpgrade       = state.intent.toUpgrade
    state.toForm          = state.intent.toForm
    state.canFight        = state.intent.canAttack
    state.canPursue       = state.intent.canPursue
    state.canCower        = state.intent.canCower
    
    actions.foreach(_.consider(state))
    
    state.shovers.clear()
  }
  
  private val actions = Vector(
    Unstick,
    Duck,
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
