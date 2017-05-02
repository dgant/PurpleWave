package Micro.Behaviors
import Lifecycle.With
import Micro.Actions._
import Micro.Intent.Intention

object Behavior {
  
  def execute(intent: Intention) {
    if ( ! readyForOrders(intent)) return
  
    intent.state.lastAction = actions.find(_.consider(intent))
  
    intent.executed = true
  }
  
  def readyForOrders(intent:Intention):Boolean = {
    // https://docs.google.com/spreadsheets/d/1bsvPvFil-kpvEUfSG74U3E5PLSTC02JxSkiR8QdLMuw/edit#gid=0
    //
    // "Dragoon, Devourer only units that can have damage by stop() too early"
    //
    if (intent.unit.attackStarting) return false
    if (intent.unit.attackAnimationHappening && intent.unit.unitClass.framesRequiredForAttackToComplete > 0) return false
    intent.unit.cooldownLeft == 0 || With.frame > intent.unit.commandFrame + intent.unit.unitClass.framesRequiredForAttackToComplete - With.latency.framesRemaining
  }
  
  val actions = Vector(
    Flee,
    Reload,
    Gather,
    Build,
    Produce,
    Target,
    Attack,
    Move
  )
}
