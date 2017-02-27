package Plans.Macro.Automatic

import Plans.Allocation.{LockUnits, LockUnitsGreedily}
import Plans.Plan
import Startup.With
import Strategies.UnitMatchers.UnitMatchWorker
import Types.UnitInfo.FriendlyUnitInfo
import Utilities.Property
import bwapi.UnitCommandType

class GatherMinerals extends Plan {
  
  val workerPlan = new Property[LockUnits](new LockUnitsGreedily { unitMatcher.set(UnitMatchWorker) })
  
  override def getChildren: Iterable[Plan] = { List(workerPlan.get) }
  
  override def onFrame() {
    workerPlan.get.onFrame()
    workerPlan.get.units
      .filterNot(worker => worker.isGatheringMinerals)
      .foreach(_orderWorker)
  }
  
  def _orderWorker(worker:FriendlyUnitInfo) {
    if (worker.isCarryingMinerals || worker.isCarryingGas) {
      //Can't spam return cargo
      if (worker.command.getUnitCommandType != UnitCommandType.Return_Cargo || ! worker.isMoving) {
        worker.baseUnit.returnCargo()
      }
    } else {
      val minerals = With.geography.ourHarvestingAreas
        .flatten(area => With.units.inRectangle(area.start.toPosition, area.end.toPosition))
        .filter(_.unitType.isMineralField)
      
      if (minerals.nonEmpty) {
        worker.baseUnit.gather(minerals.minBy(_.position.getDistance(worker.position)).baseUnit)
      }
    }
  }
}
