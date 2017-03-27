package Micro.Behaviors
import Micro.Intentions.Intention
import Startup.With

object BehaviorWorker extends Behavior {
  
  def execute(intent: Intention) {
    
    intent.movementProfileNormal = MovementProfiles.worker
  
    if (intent.toBuild.isDefined) {
      return With.commander.build(intent, intent.toBuild.get, intent.destination.get)
    }
  
    if (intent.toGather.isDefined) {
      return With.commander.gather(intent, intent.toGather.get)
    }
    
    BehaviorDefault.execute(intent)
  }
}
