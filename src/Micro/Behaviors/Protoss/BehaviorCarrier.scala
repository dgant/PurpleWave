package Micro.Behaviors.Protoss

import Micro.Behaviors.{Behavior, BehaviorDefault, MovementProfiles}
import Micro.Intentions.Intention
import Startup.With

object BehaviorCarrier extends Behavior {
  
  override def execute(intent: Intention) {
    
    intent.movementProfileCombat = MovementProfiles.carrier
    intent.movementProfileNormal = MovementProfiles.carrier
    
    if (intent.unit.trainingQueue.isEmpty && intent.unit.interceptors < (if(intent.targets.isEmpty) 8 else 2)) {
      return With.commander.buildInterceptor(intent)
    }
    
    return BehaviorDefault.execute(intent)
  }
}
