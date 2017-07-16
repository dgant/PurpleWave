package Micro.Actions

import Micro.Actions.Basic._
import Micro.Actions.Combat.{Duck, Fight, Smorc}
import Micro.Actions.Commands.{Attack, Travel}
import Micro.Actions.Protoss.Meld
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
    state.canFlee         = state.intent.canFlee
    state.canPursue       = state.intent.canPursue
    state.canCower        = state.intent.canCower
    state.canMeld         = state.intent.canMeld
    
    actions.foreach(_.consider(state))
    
    state.shovers.clear()
  }
  
  private val actions = Vector(
    Cancel, //Probably not actually used yet because candidates won't be in the Executor queue
    Meld,
    Smorc,
    Duck,
    Gather,
    Build,
    Unstick, //Workers don't seem to ever get stuck
    Produce,
    ReloadInterceptors,
    ReloadScarabs,
    Fight,
    Attack,
    Travel
  )
}
