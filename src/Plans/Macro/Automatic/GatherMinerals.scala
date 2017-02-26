package Plans.Macro.Automatic

import Plans.Allocation.{LockUnits, LockUnitsGreedily}
import Plans.Plan
import Startup.With
import Strategies.UnitMatchers.UnitMatchWorker
import Utilities.Property
import bwapi.UnitCommandType

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
  
  def _orderWorker(worker:bwapi.Unit) {
    if (worker.isCarryingMinerals || worker.isCarryingGas) {
      //Can't spam return cargo
      if (worker.getLastCommand.getUnitCommandType != UnitCommandType.Return_Cargo || ! worker.isMoving) {
        worker.returnCargo()
      }
    } else {
      val minerals = With.geography.ourHarvestingAreas
        .flatten(area => With.game.getUnitsInRectangle(area.start.toPosition, area.end.toPosition).asScala)
        .filter(_.getType.isMineralField)
      
      if (minerals.nonEmpty) {
        worker.gather(minerals.minBy(_.getDistance(worker)))
      }
    }
  }
}
