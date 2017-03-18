package Micro.Behaviors
import Micro.Intentions.Intention
import Startup.With

object BehaviorBuilding extends Behavior {
  
  def execute(intent: Intention) {
    if (intent.toBuild.isDefined) {
      return With.commander.build(intent, intent.toBuild.get)
    }
    if (intent.toTech.isDefined) {
      return With.commander.tech(intent, intent.toTech.get)
    }
    if (intent.toUpgrade.isDefined) {
      return With.commander.upgrade(intent, intent.toUpgrade.get)
    }
  }
}
