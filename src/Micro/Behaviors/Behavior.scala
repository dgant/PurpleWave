package Micro.Behaviors
import Micro.Actions._
import Micro.Intent.Intention
import ProxyBwapi.Races.Protoss

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
    ! (intent.unit.attackStarting || (intent.unit.attackAnimationHappening && intent.unit.is(Protoss.Dragoon)))
  }
  
  val actions = Vector(
    Target,
    Flee,
    Reload,
    Kite,
    Gather,
    Build,
    Produce,
    Attack,
    Move
  )
}
