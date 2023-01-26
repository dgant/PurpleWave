package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Produce extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    (unit.intent.toTrain.isDefined
      || unit.intent.toTech.isDefined
      || unit.intent.toUpgrade.isDefined)
      && (unit.trainingQueue.isEmpty || unit.trainee.exists(_.remainingCompletionFrames <= 1 + With.latency.latencyFrames + With.reaction.agencyMax))
      && unit.trainingQueue.size < 2)
  
  override def perform(unit: FriendlyUnitInfo): Unit = {
    if (unit.intent.toTrain.isDefined) {
      if (With.framesSince(unit.intent.frameCreated) < Math.max(128, unit.intent.toTrain.get.buildFrames / 2)) {
        Commander.build(unit, unit.intent.toTrain.get)
      }
    } else if (unit.intent.toTech.isDefined) {
      Commander.tech(unit, unit.intent.toTech.get)
    } else if (unit.intent.toUpgrade.isDefined) {
      Commander.upgrade(unit, unit.intent.toUpgrade.get)
    }
    unit.intent.toTrain = None // Avoid training repeatedly
    unit.intent.toTech = None // Avoid teching repeatedly (mostly impacts failure to renew the desire to tech)
    unit.intent.toUpgrade = None //Avoid upgrading repeatedly
  }
}
