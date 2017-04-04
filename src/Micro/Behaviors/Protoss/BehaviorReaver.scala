package Micro.Behaviors.Protoss

import Micro.Behaviors.{Behavior, BehaviorDefault}
import Micro.Intentions.Intention
import Lifecycle.With

object BehaviorReaver extends Behavior {
  
  override def execute(intent: Intention) {
    if (intent.unit.trainingQueue.isEmpty && intent.unit.scarabs < (if(intent.targets.isEmpty) 5 else 1)) {
      return With.commander.buildScarab(intent)
    }
    
    return BehaviorDefault.execute(intent)
  }
}
