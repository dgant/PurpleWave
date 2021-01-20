package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object ReloadInterceptors extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean =(
    unit.is(Protoss.Carrier)
    && With.self.minerals > Protoss.Interceptor.mineralPrice
    && unit.interceptors.size < (if (With.self.hasUpgrade(Protoss.CarrierCapacity)) 8 else 4)
    && unit.trainingQueue.size < 2
    && unit.trainee.forall(_.remainingCompletionFrames < With.reaction.agencyMax)
    && unit.matchups.framesToLive > Protoss.Interceptor.buildFrames + 48.0
  )
  
  override def perform(unit: FriendlyUnitInfo) {
    Commander.buildInterceptor(unit)
  }
}
