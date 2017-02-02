package Plans.Strategy.Protoss

import Plans.Generic.Compound.PlanDelegateInSerial
import Plans.Generic.Macro.PlanFollowBuildOrder

class ProtossCheese extends PlanDelegateInSerial {
  
  var _proxyGateways = new PlanFollowBuildOrder
  
}
