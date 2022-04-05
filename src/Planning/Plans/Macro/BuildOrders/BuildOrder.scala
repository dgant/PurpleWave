package Planning.Plans.Macro.BuildOrders

import Lifecycle.With
import Macro.Requests.{RequestBuildable, RequestUnit}
import Planning.Plan

/**
  * Requests buildables without replacing non-tech units
  */
class BuildOrder(requests: RequestBuildable*) extends Plan {
  override def onUpdate() {
    val modifiedRequests = requests.flatMap(request =>
      if (request.unit.forall(_.isBuilding) || request.quantity <= 0) Some(request) else {
        val unit = request.unit.get
        val quantityLost = Math.max(0, With.productionHistory.doneAllTime(unit) - With.macroCounts.oursComplete(unit))
        val quantityToRequest = request.quantity - quantityLost
        Some(RequestUnit(unit, quantityToRequest)).filter(_.quantity > 0)
      })
    With.scheduler.requestAll(this, modifiedRequests)
  }
}
