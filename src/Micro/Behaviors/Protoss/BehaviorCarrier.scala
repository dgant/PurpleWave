package Micro.Behaviors.Protoss

import Micro.Behaviors.{Behavior, BehaviorDefault}
import Micro.Intentions.Intention
import Startup.With

object BehaviorCarrier extends Behavior {
  
  override def execute(intent: Intention) {
    if (intent.unit.trainingQueue.isEmpty && intent.unit.interceptors < (if(intent.targets.isEmpty) 8 else 1)) {
      return With.commander.buildInterceptor(intent)
    }
    
    return BehaviorDefault.execute(intent)
  }
}
