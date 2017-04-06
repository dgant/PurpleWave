package Micro.Behaviors.General

import Lifecycle.With
import Micro.Behaviors.{Behavior, BehaviorDefault}
import Micro.Intent.Intention

object BehaviorWorker extends Behavior {
  
  def execute(intent: Intention) {
  
    if (intent.toBuild.isDefined) {
      return With.commander.build(intent, intent.toBuild.get, intent.destination.get)
    }
  
    if (intent.toGather.isDefined) {
      //TODO: Test this against heuristics when threats exist, so that we don't blindly transfer workers into a fight
      return With.commander.gather(intent, intent.toGather.get)
    }
    
    BehaviorDefault.execute(intent)
  }
}
