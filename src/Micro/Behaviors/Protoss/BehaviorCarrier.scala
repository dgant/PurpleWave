package Micro.Behaviors.Protoss

import Micro.Behaviors.{Behavior, BehaviorDefault, MovementProfiles}
import Micro.Intent.Intention
import Lifecycle.With

object BehaviorCarrier extends Behavior {
  
  override def execute(intent: Intention) {
    
    intent.movementProfile = MovementProfiles.carrier
    
    //This will be stupid if we don't have the capacity upgrade
    if (intent.unit.trainingQueue.isEmpty && intent.unit.interceptors < 8) {
      return With.commander.buildInterceptor(intent)
    }
    
    return BehaviorDefault.execute(intent)
  }
}
