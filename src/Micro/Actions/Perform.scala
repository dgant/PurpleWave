package Micro.Actions
import Lifecycle.With
import Micro.Intent.Intention

object Perform extends Action {
  
  override def perform(intent: Intention): Boolean = {
    
    if (intent.unit.trainingQueue.isEmpty) {
      if (intent.toBuild.isDefined) {
        With.commander.build(intent, intent.toBuild.get)
        return true
      }
      if (intent.toTech.isDefined) {
        With.commander.tech(intent, intent.toTech.get)
        return true
      }
      if (intent.toUpgrade.isDefined) {
        With.commander.upgrade(intent, intent.toUpgrade.get)
        return true
      }
    }
    
    false
  }
}
