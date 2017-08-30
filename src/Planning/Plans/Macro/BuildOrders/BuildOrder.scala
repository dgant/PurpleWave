package Planning.Plans.Macro.BuildOrders

import Lifecycle.With
import Macro.BuildRequests.{BuildRequest, RequestAtLeast}
import Planning.Composition.Property
import Planning.Plan
import ProxyBwapi.UnitClass.UnitClass

import scala.collection.mutable

class BuildOrder(initialRequests: BuildRequest*) extends Plan {
  
  // Follow a build order,
  // in which we rebuild missing buildings
  // but not missing units
  
  val requests = new Property[Seq[BuildRequest]](initialRequests)
  
  private val countByClassAllTime = new mutable.HashMap[UnitClass, mutable.HashSet[Int]] {
    override def default(key: UnitClass): mutable.HashSet[Int] = {
      if ( ! contains(key)) put(key, new mutable.HashSet[Int])
      this(key)
    }
  }
  
  private def done(unitClass: UnitClass) = countByClassAllTime(unitClass).size
  
  override def onUpdate() {
    
    With.units.ours.foreach(unit => countByClassAllTime(unit.unitClass).add(unit.id))
    val countByClassNow = With.units.ours.groupBy(_.unitClass).map(pair => (pair._1, pair._2.size))
  
    val modifiedRequests = requests.get.flatMap(request => {
      val unit = request.buildable.unitOption.filter( ! _.isBuilding)
      if (unit.isDefined && request.require > 0) {
        val quantityNow = countByClassNow.getOrElse(unit.get, 0)
        val quantityNew = request.require + quantityNow - done(unit.get)
        if (quantityNew <= 0) {
          None
        }
        else {
          Some(RequestAtLeast(quantityNew, unit.get))
        }
      } else {
        Some(request)
      }
    })
    
    With.scheduler.request(this, modifiedRequests)
  }
}
