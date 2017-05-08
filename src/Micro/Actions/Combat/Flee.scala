package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Actions.Commands.{Reposition, Travel}
import Micro.Intent.Intention

object Flee extends Action {
  
  override def allowed(intent: Intention) = {
    intent.unit.canMoveThisFrame
  }
  
  override def perform(intent: Intention) {
    
    intent.canPursue = false
    intent.destination = Some(intent.origin)
  
    val enemyFaster = intent.threatsActive.exists(threat => threat.topSpeed > intent.unit.topSpeed)
    val weAreFaster = intent.threatsActive.forall(threat => threat.topSpeed < intent.unit.topSpeed)
  
    // If the enemy is faster, go straight home so we don't get caught
    if (enemyFaster) {
      Travel.consider(intent)
    }
  
    //If we're faster, we can be cuter with how we retreat
    if (weAreFaster) {
      Reposition.consider(intent)
    }
  
    // If we have a clear path home, then skip heuristic movement and just go.
    val ourDistanceToOrigin = intent.unit.pixelDistanceTravelling(intent.origin) - 32.0
    if (intent.threatsActive.forall(threat =>
      ourDistanceToOrigin <= (
        if (intent.unit.flying) threat.pixelDistanceFast(intent.origin) //Don't retreat directly over the enmy!
        else                    threat.pixelDistanceTravelling(intent.origin)))) {
    
      Travel.consider(intent)
    }
  
    Reposition.consider(intent)
  }
}
