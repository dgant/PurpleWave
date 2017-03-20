package Micro.Behaviors.Protoss

import Micro.Behaviors.{Behavior, BehaviorDefault, MovementProfiles}
import Micro.Intentions.Intention

object BehaviorCorsair extends Behavior {
  def execute(intent: Intention) {
    intent.movementProfileCombat = MovementProfiles.corsair
    intent.movementProfileNormal = MovementProfiles.corsair
    
    BehaviorDefault.execute(intent)
  }
}
