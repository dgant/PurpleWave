package Planning.Plans.Macro.BuildOrders

import Lifecycle.With
import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Composition.Property
import Planning.Plan

class BuildOrder(initialRequests: BuildRequest*) extends Plan {
  
  // Follow a build order,
  // in which we rebuild missing buildings
  // but not missing units
  
  val requests = new Property[Seq[BuildRequest]](initialRequests)
  
  override def onUpdate() {
    val countByClassNow = With.units.ours.groupBy(_.unitClass).map(pair => (pair._1, pair._2.size))
  
    val modifiedRequests = requests.get.flatMap(request => {
      val unit = request.buildable.unitOption.filter( ! _.isBuilding)
      if (unit.isDefined && request.require > 0) {
        val quantityNow = countByClassNow.getOrElse(unit.get, 0)
        val quantityNew = request.require + quantityNow - With.buildOrderHistory.doneAllTime(unit.get)
        if (quantityNew <= 0) {
          None
        }
        else {
          Some(Get(quantityNew, unit.get))
        }
      } else {
        Some(request)
      }
    })
    
    With.scheduler.request(this, modifiedRequests)
  }
}
