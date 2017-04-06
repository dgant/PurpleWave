package Micro.Behaviors
import Lifecycle.With
import Micro.Actions.{Attack, Flee, Move, Pursue}
import Micro.Intent.Intention

object BehaviorDefault extends Behavior {
  
  def execute(intent: Intention) {
    if ( ! readyForOrders(intent)) return
    
    intent.desireToFight = With.battles.byUnit.get(intent.unit)
      .map(battle => (1.0 + battle.us.strength) / (1.0 + battle.enemy.strength))
      .getOrElse(1.0)
    
    Flee.perform(intent)    ||
    Pursue.perform(intent)  ||
    Attack.perform(intent)  ||
    Move.perform(intent)
  }
  
  def readyForOrders(intent:Intention):Boolean = {
    ! intent.unit.attackStarting && ! intent.unit.attackAnimationHappening
  }
}
