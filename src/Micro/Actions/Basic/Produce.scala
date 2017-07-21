package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Produce extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.trainingQueue.isEmpty
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    
    if (unit.action.toTrain.isDefined) {
      With.commander.build(unit, unit.action.toTrain.get)
      unit.action.intent.toTrain = None //Avoid building repeatedly
    }
    else if (unit.action.toTech.isDefined) {
      With.commander.tech(unit, unit.action.toTech.get)
    }
    else if (unit.action.toUpgrade.isDefined) {
      With.commander.upgrade(unit, unit.action.toUpgrade.get)
    }
  }
}
