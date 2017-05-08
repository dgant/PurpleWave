package Micro.Actions

import Lifecycle.With
import Micro.Actions.Basic._
import Micro.Actions.Commands.{Attack, Travel}
import Micro.Intent.Intention

object Idle extends Action {
  
  override def allowed(intent:Intention):Boolean = {
    
    // https://docs.google.com/spreadsheets/d/1bsvPvFil-kpvEUfSG74U3E5PLSTC02JxSkiR8QdLMuw/edit#gid=0
    //
    // "Dragoon, Devourer only units that can have damage by stop() too early"
    //
    if (intent.unit.attackStarting) {
      return false
    }
    if (intent.unit.attackAnimationHappening && intent.unit.unitClass.framesRequiredForAttackToComplete > 0){
      return false
    }
    intent.unit.cooldownLeft == 0 ||
      With.frame > intent.unit.commandFrame + intent.unit.unitClass.framesRequiredForAttackToComplete - With.latency.framesRemaining
  }
  
  def perform(intent: Intention) {
    actions.foreach(_.consider(intent))
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
