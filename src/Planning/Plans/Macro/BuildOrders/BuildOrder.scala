package Planning.Plans.Macro.BuildOrders

import Lifecycle.With
import Macro.BuildRequests.{BuildRequest, Get}
import Macro.Scheduling.MacroCounter
import Planning.{Plan, Property}

class BuildOrder(initialRequests: BuildRequest*) extends Plan {
  
  // Follow a build order,
  // in which we rebuild missing buildings
  // but not missing units
  
  val requests = new Property[Seq[BuildRequest]](initialRequests)
  
  override def onUpdate() {
    val modifiedRequests = requests.get.flatMap(request => {
      val unit = request.buildable.unitOption.filter( ! _.isBuilding)
      if (unit.isDefined && request.total > 0) {
        val quantityLost = Math.max(0, With.buildOrderHistory.doneAllTime(unit.get) - MacroCounter.countFriendlyCompleteOrIncomplete(unit.get))
        val quantityToRequest = request.total - quantityLost
        if (quantityToRequest > 0) {
          Some(Get(quantityToRequest, unit.get))
        } else {
          None
        }
      } else {
        Some(request)
      }
    })
    
    modifiedRequests.foreach(With.scheduler.request(this, _))
  }
}
