package Plans.Generic.Macro

import Startup.With
import Plans.Generic.Allocation.PlanAcquireUnitsGreedily
import Plans.Plan
import UnitMatchers.UnitMatchWorker

import scala.collection.JavaConverters._

class PlanGatherMinerals extends Plan {
  
  val _workerPlan = new PlanAcquireUnitsGreedily(UnitMatchWorker)
  _children = List(_workerPlan)
  
  var _mineral:Option[bwapi.Unit] = None
  
  override def execute() {
    if (_mineral.isEmpty) {
      _mineral = With.game.getMinerals.asScala.filter(_.isVisible).headOption
    }
  
    if ( ! _mineral.isEmpty) {
      _workerPlan.execute()
      _workerPlan.units
        .filterNot(worker => worker.isGatheringMinerals)
        .foreach(worker => worker.gather(_mineral.head))
    }
  }
}
