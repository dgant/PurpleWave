package Micro.Behaviors
import Micro.Actions._
import Micro.Intent.Intention

object Behavior {
  
  def execute(intent: Intention) {
    if ( ! readyForOrders(intent)) return
  
    actions.find(action => action.allowed(intent) && action.perform(intent))
  
    intent.executed = true
  }
  
  def readyForOrders(intent:Intention):Boolean = {
    ! intent.unit.attackStarting && ! intent.unit.attackAnimationHappening
  }
  
  val actions = List(
    Flee,
    Kite,
    Pursue,
    Kite,
    Reload,
    Gather,
    Build,
    Produce,
    Attack,
    Move
  )
}
