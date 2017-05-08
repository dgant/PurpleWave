package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Flee
import Micro.Behaviors.MovementProfiles
import Micro.Intent.Intention

object Gather extends Action {
  
  override def allowed(intent: Intention) = {
    intent.toGather.isDefined
  }
  
  override def perform(intent: Intention) {
  
    // If we're threatened and continuing to gather won't help, respond
    if (
      intent.threatsActive.exists(threat =>
      threat.pixelDistanceFast(intent.unit) >=
      threat.pixelDistanceFast(intent.toGather.get))) {
    
      intent.movementProfile  = MovementProfiles.flee
      intent.toGather         = None
      intent.canAttack        &&= intent.threatsActive.map(_.dpsAgainst(intent.unit)).sum < intent.unit.totalHealth
    
      Flee.consider(intent)
    }
    
    With.commander.gather(intent.unit, intent.toGather.get)
  }
}
