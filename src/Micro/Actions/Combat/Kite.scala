package Micro.Actions.Combat

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Commands.Reposition
import Micro.Intent.Intention

object Kite extends Action {
  
  override def allowed(intent: Intention): Boolean = (
    intent.unit.canMoveThisFrame
    && intent.targets.nonEmpty
    && intent.unit.pixelRangeMax > 32 * 3.0
  )
  
  override def perform(intent: Intention) {
    
    if (intent.unit.cooldownLeft > 0) {
      Reposition.delegate(intent)
      return
    }
    
    // If we can kite perfectly, do so
    // If we can't, just try to maximize the damage we deal compared to damage received
    
    val weAreFaster   = intent.threatsActive.forall(_.topSpeed <= intent.unit.topSpeed)
    val enemyIsFaster = intent.threatsActive.exists(_.topSpeed > intent.unit.topSpeed)
    
    //Should really be "number of frames it takes to attack and accelerate back to top speed"
    val framesToAttackAndResumeMovement = With.configuration.microFrameLookahead
  
    // If we can kite perfectly without taking damage (vultures/dragoons vs. slow zealots, for instance), let's try to do so.
    // This might backfire in closed spaces, by causing us to not fire when we otherwise could
    if (weAreFaster && intent.threatsActive.exists(_.framesBeforeAttacking(intent.unit) <= framesToAttackAndResumeMovement)) {
      Reposition.delegate(intent)
    }
    else {
      Engage.delegate(intent)
    }
  }
}
