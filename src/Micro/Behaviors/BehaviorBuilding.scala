package Micro.Behaviors
import Micro.Intentions.Intention
import Startup.With

object BehaviorBuilding extends Behavior {
  
  def execute(intent: Intention) {
    if (intent.toBuild.isDefined) {
      return With.commander.build(intent, intent.toBuild.get)
    }
  }
}
