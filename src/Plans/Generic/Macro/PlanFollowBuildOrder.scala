package Plans.Generic.Macro

import Plans.Generic.Compound.PlanDelegateInParallel
import Plans.Plan
import Types.BuildOrders.Buildable
import Types.BuildOrders.Protoss.BuildProxyGateway

class PlanFollowBuildOrder extends PlanDelegateInParallel {
  
  val buildOrder = new BuildProxyGateway
  _children = buildOrder.orders.map(_createABuildPlan)
  
  def _createABuildPlan(buildable:Buildable):Plan = {
    
    if (buildable.unitType != null) {
      if (buildable.unitType.isBuilding) {
        return new PlanBuildBuilding(buildable.unitType) { if (buildable.positionFinder != null) _positionFinder = buildable.positionFinder }
      }
      else {
        return new PlanTrainUnit(buildable.unitType)
      }
    }
    
    throw new Exception("I don't know how to build that!")
  }
}
