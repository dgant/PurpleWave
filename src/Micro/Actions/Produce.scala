package Micro.Actions
import Lifecycle.With
import Micro.Intent.Intention

object Produce extends Action {
  
  override def allowed(intent: Intention) = {
    intent.unit.trainingQueue.isEmpty
  }
  
  override def perform(intent: Intention): Boolean = {
    
    if (intent.toTrain.isDefined) {
      With.commander.build(intent, intent.toTrain.get)
      intent.toTrain = None //Avoid repeating the build
      return true
    }
    if (intent.toTech.isDefined) {
      With.commander.tech(intent, intent.toTech.get)
      intent.toTech = None //Avoid repeating the build
      return true
    }
    if (intent.toUpgrade.isDefined) {
      With.commander.upgrade(intent, intent.toUpgrade.get)
      intent.toUpgrade = None //Avoid repeating the build
      return true
    }
    
    false
  }
}
