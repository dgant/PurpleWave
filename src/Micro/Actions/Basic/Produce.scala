package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import Micro.Intent.Intention

object Produce extends Action {
  
  override def allowed(intent: Intention) = {
    intent.unit.trainingQueue.isEmpty
  }
  
  override def perform(intent: Intention) {
    
    if (intent.toTrain.isDefined) {
      With.commander.build(intent.unit, intent.toTrain.get)
      intent.toTrain = None //Avoid building repeatedly
    }
    else if (intent.toTech.isDefined) {
      With.commander.tech(intent.unit, intent.toTech.get)
    }
    else if (intent.toUpgrade.isDefined) {
      With.commander.upgrade(intent.unit, intent.toUpgrade.get)
    }
  }
}
