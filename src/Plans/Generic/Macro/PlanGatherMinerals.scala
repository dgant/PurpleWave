package Plans.Generic.Macro

import Plans.Generic.Allocation.PlanAcquireUnitsGreedily
import Plans.Plan
import Traits.TraitSettableChildren
import Startup.With
import Strategies.UnitMatchers.UnitMatchWorker

import scala.collection.JavaConverters._

class PlanGatherMinerals
  extends Plan
  with TraitSettableChildren {
  
  val _workerPlan = new PlanAcquireUnitsGreedily(UnitMatchWorker)
  setChildren(List(_workerPlan))
  
  var _mineral:Option[bwapi.Unit] = None
  
  override def onFrame() {
    if (_mineral.isEmpty) {
      _mineral = With.game.getMinerals.asScala.filter(_.isVisible).headOption
    }
  
    if ( ! _mineral.isEmpty) {
      _workerPlan.onFrame()
      _workerPlan.units
        .filterNot(worker => worker.isGatheringMinerals)
        .foreach(worker => worker.gather(_mineral.head))
    }
  }
}
