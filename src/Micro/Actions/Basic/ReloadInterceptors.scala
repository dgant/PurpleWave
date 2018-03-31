package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object ReloadInterceptors extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean =(
    unit.is(Protoss.Carrier)
    && With.self.minerals > Protoss.Interceptor.mineralPrice
    && unit.interceptorCount < (if (With.self.hasUpgrade(Protoss.CarrierCapacity)) 8 else 4)
    && unit.trainingQueue.isEmpty
    && unit.matchups.framesToLiveDiffused > Protoss.Interceptor.buildFrames + 48.0
  )
  
  override def perform(unit: FriendlyUnitInfo) {
    With.commander.buildInterceptor(unit)
  }
}
