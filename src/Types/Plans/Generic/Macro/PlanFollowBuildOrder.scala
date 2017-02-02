package Types.Plans.Generic.Macro

import Types.BuildOrders.BuildOrder
import Types.Plans.Generic.Compound.PlanDelegateInParallel
import Types.Plans.Plan
import bwapi.UnitType

class PlanFollowBuildOrder extends PlanDelegateInParallel {
  
  val buildOrder = new BuildOrder
  _children = buildOrder.orders.map(_createABuildPlan)
  
  def _createABuildPlan(product: UnitType):Plan = {
    
    if (product.isBuilding) {
      return new PlanBuildBuilding(product)
    }
    else {
      return new PlanTrainUnit(product)
    }
    
    throw new Exception("Don't know how to build this yet.")
  }
}
