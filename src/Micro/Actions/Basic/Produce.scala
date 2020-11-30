package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Produce extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    (
      unit.agent.toTrain.isDefined  ||
      unit.agent.toTech.isDefined   ||
      unit.agent.toUpgrade.isDefined
    )
    && (unit.trainingQueue.isEmpty || (unit.trainingQueue.size == 1 && unit.remainingTrainFrames < With.reaction.agencyMax))

  )
  
  override def perform(unit: FriendlyUnitInfo) {
    
    if (unit.agent.toTrain.isDefined) {
      if (With.framesSince(unit.agent.intent.frameCreated) < Math.max(128, unit.agent.toTrain.get.buildFrames / 2)) {
        With.commander.build(unit, unit.agent.toTrain.get)
      }
    }
    else if (unit.agent.toTech.isDefined) {
      With.commander.tech(unit, unit.agent.toTech.get)
    }
    else if (unit.agent.toUpgrade.isDefined) {
      With.commander.upgrade(unit, unit.agent.toUpgrade.get)
    }

      unit.agent.intent.toTrain = None // Avoid training repeatedly
    unit.agent.intent.toTech = None // Avoid teching repeatedly (mostly impacts failure to renew the desire to tech)
    unit.agent.intent.toUpgrade = None //Avoid upgrading repeatedly
  }
}
