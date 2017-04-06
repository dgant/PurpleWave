package Micro.Behaviors.Protoss

import Micro.Behaviors.{Behavior, BehaviorDefault, MovementProfiles}
import Micro.Intent.Intention

object BehaviorCorsair extends Behavior {
  def execute(intent: Intention) {
    
    intent.movementProfile = MovementProfiles.corsair
    
    BehaviorDefault.execute(intent)
    
  }
}
