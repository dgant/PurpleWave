package Micro.Actions

import Lifecycle.With
import Micro.Actions.Basic._
import Micro.Actions.Commands.{Attack, Travel}
import Micro.State.ExecutionState

object Idle extends Action {
  
  override def allowed(state:ExecutionState):Boolean = {
    
    // https://docs.google.com/spreadsheets/d/1bsvPvFil-kpvEUfSG74U3E5PLSTC02JxSkiR8QdLMuw/edit#gid=0
    //
    // "Dragoon, Devourer only units that can have damage by stop() too early"
    //
    if (state.unit.attackStarting) {
      return false
    }
    if (state.unit.attackAnimationHappening && state.unit.unitClass.framesRequiredForAttackToComplete > 0) {
      return false
    }
    state.unit.cooldownLeft == 0 ||
      With.frame > state.unit.commandFrame + state.unit.unitClass.framesRequiredForAttackToComplete
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
