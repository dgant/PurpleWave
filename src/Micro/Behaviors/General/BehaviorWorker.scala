package Micro.Behaviors.General

import Micro.Actions.{Build, Gather}
import Micro.Behaviors.{Behavior, BehaviorDefault}
import Micro.Intent.Intention

object BehaviorWorker extends Behavior {
  
  def execute(intent: Intention) {
  
    if (Gather.perform(intent) || Build.perform(intent)) {
      return
    }
    
    BehaviorDefault.execute(intent)
  }
}
