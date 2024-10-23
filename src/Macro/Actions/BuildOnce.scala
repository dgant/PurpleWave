package Macro.Actions

import Lifecycle.With
import Macro.Requests.{RequestBuildable, RequestUnit}

object BuildOnce {
  def apply(requester: Any, request: RequestBuildable): Unit = {
    With.scheduler.request(
      requester,
        if (request.unit.forall(_.isBuilding) || request.quantity <= 0) request else {
        val unit              = request.unit.get
        val quantityLost      = Math.max(0, With.productionHistory.doneAllTime(unit) - With.macroCounts.oursComplete(unit))
        val quantityToRequest = request.quantity - quantityLost
        RequestUnit(unit, quantityToRequest)
      })
  }
}
