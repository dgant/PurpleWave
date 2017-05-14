package Micro.Actions

import Lifecycle.With
import Micro.Actions.Basic._
import Micro.Actions.Commands.{Attack, Travel}
import Micro.Task.ExecutionState

object Idle extends Action {
  
  override def allowed(state:ExecutionState):Boolean = {
    
    if (state.unit.attackStarting) {
      return false
    }
  
    // https://docs.google.com/spreadsheets/d/1bsvPvFil-kpvEUfSG74U3E5PLSTC02JxSkiR8QdLMuw/edit#gid=0
    //
    // "Dragoon, Devourer only units that can have damage prevented by stop() too early"
    //
    // According to JohnJ: "After the frame where isStartingAttack is true, it should be left alone for the next 8 frames"
    // "I assume that frame count is in ignorance of latency, so if i issue an order before the 8 frames are up that would be executed on the first frame thereafter, i'm in the clear?"
    // JohnJ: yeah in theory. actually in practice. I did test that.
    //
    if (state.unit.unitClass.framesRequiredForAttackToComplete > 0) {
      if (state.unit.attackAnimationHappening) {
        return false
      }
      if (state.unit.cooldownLeft == 0) {
        return true
      }
      
      return With.frame > state.unit.commandFrame + state.unit.unitClass.framesRequiredForAttackToComplete
    }
    
    true
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
