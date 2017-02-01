package Types.Plans.Generic.Macro

import Types.BuildOrders.BuildOrder
import Types.Plans.Generic.Compound.PlanDelegateInParallel
import Types.Plans.Plan
import bwapi.UnitType

class PlanFollowBuildOrder extends PlanDelegateInParallel {
  
  val buildOrder = new BuildOrder
  _children = buildOrder.orders.map(_createABuildPlan)
  
  def _createABuildPlan(product: UnitType):Plan = {
    val builder = product.whatBuilds.first
    
    if (builder.isBuilding) {
      return new PlanTrainUnitFromBuilding(builder, product)
    }
    if (builder.isWorker) {
      return new PlanBuildBuildingWithWorker(builder, product)
    }
    
    throw new Exception("Don't know how to build this yet.")
  }
}
