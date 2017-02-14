package Plans.Generic.Macro

import Plans.Generic.Allocation.{LockUnits, LockUnitsGreedily}
import Plans.Plan
import Startup.With
import Strategies.UnitMatchers.UnitMatchWorker
import Types.Property

import scala.collection.JavaConverters._

class GatherMinerals extends Plan {
  
  val workerPlan = new Property[LockUnits](new LockUnitsGreedily { unitMatcher.set(UnitMatchWorker) })
  
  override def getChildren: Iterable[Plan] = { List(workerPlan.get) }
  
  override def onFrame() {
    workerPlan.get.onFrame()
    workerPlan.get.units
      .filterNot(worker => worker.isGatheringMinerals)
      .foreach(_orderWorker)
  }
  
  def _orderWorker(unit:bwapi.Unit) {
    if (unit.isCarryingMinerals || unit.isCarryingGas) {
      unit.returnCargo()
    } else {
      val minerals = With.map.ourHarvestingAreas
        .flatten(area => With.game.getUnitsInRectangle(area.start.toPosition, area.end.toPosition).asScala)
        .filter(_.getType.isMineralField)
      
      if (minerals.nonEmpty) {
        unit.gather(minerals.minBy(_.getDistance(unit)))
      }
    }
  }
}
