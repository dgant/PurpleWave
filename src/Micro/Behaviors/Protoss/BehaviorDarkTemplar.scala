package Micro.Behaviors.Protoss

import Micro.Behaviors.{Behavior, BehaviorDefault, MovementProfiles}
import Micro.Intent.Intention

object BehaviorDarkTemplar extends Behavior {
  
  def execute(intent: Intention) {
    
    intent.movementProfile = MovementProfiles.darkTemplar
  
    BehaviorDefault.execute(intent)
    
  }
}
