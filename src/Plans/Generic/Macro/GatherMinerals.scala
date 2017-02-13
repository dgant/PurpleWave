package Plans.Generic.Macro

import Plans.Generic.Allocation.{LockUnits, LockUnitsGreedily}
import Plans.Plan
import Startup.With
import Strategies.UnitMatchers.UnitMatchWorker
import Types.Property

import scala.collection.JavaConverters._

class GatherMinerals extends Plan {
  
  val workerPlan = new Property[LockUnits](new LockUnitsGreedily { unitMatcher.set(UnitMatchWorker) })
  var _mineral:Option[bwapi.Unit] = None
  
  override def getChildren: Iterable[Plan] = { List(workerPlan.get) }
  
  override def onFrame() {
    if (_mineral.isEmpty) {
      _mineral = With.game.getMinerals.asScala
        .filter(_.isVisible)
        .sortBy(mineral => With.map.ourBaseHalls.map(_.getPosition.getDistance(mineral.getPosition)).headOption.getOrElse(0.0))
        .headOption
    }
  
    if ( ! _mineral.isEmpty) {
      workerPlan.get.onFrame()
      workerPlan.get.units
        .filterNot(worker => worker.isGatheringMinerals)
        .foreach(worker => _orderWorker(worker, _mineral.head))
    }
  }
  
  def _orderWorker(unit:bwapi.Unit, mineral:bwapi.Unit) {
    if (unit.isCarryingMinerals || unit.isCarryingGas) {
      unit.returnCargo()
    } else {
      unit.gather(mineral)
    }
  }
}
