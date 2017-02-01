package Types.Plans.Generic.Macro

import Startup.With
import Types.Plans.Generic.Allocation.PlanAcquireUnitsGreedily
import Types.Plans.Plan
import Types.Tactics.{Tactic, TacticGatherMinerals}
import UnitMatchers.UnitMatchWorker

import scala.collection.mutable

class PlanGatherMinerals extends Plan {
  
  val _workerPlan = new PlanAcquireUnitsGreedily(UnitMatchWorker)
  _children = List(_workerPlan)
  
  val _workerTactics:mutable.Map[bwapi.Unit, Tactic] = mutable.Map.empty
  
  override def execute() {
    _workerPlan.execute()
    
    _workerTactics.keySet.diff(_workerPlan.units).foreach(_workerTactics.remove)
    _workerPlan.units
      .filterNot(_workerTactics.contains)
      .foreach(worker => _workerTactics.put(worker, new TacticGatherMinerals(worker)))
    
    _workerTactics.values.foreach(With.commander.enqueue)
  }
}
