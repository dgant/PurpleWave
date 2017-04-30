package Micro.Behaviors
import Micro.Actions._
import Micro.Intent.Intention

object Behavior {
  
  def execute(intent: Intention) {
    if ( ! readyForOrders(intent)) return
  
    intent.state.lastAction = actions.find(action => action.allowed(intent) && action.perform(intent))
  
    intent.executed = true
  }
  
  def readyForOrders(intent:Intention):Boolean = {
    ! intent.unit.attackStarting && ! intent.unit.attackAnimationHappening
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
