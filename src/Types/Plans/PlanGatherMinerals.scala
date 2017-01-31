package Types.Plans

import Startup.With
import Types.Requirements.{PriorityMinimum, RequireUnits}
import Types.Tactics.{Tactic, TacticGatherMinerals}
import UnitMatching.Matcher.UnitMatchWorker

import scala.collection.mutable

class PlanGatherMinerals extends Plan {
  
  override val requirementsOptimal:RequireUnits = new RequireUnits(this, PriorityMinimum, UnitMatchWorker, Integer.MAX_VALUE)
  
  val _tactics:mutable.Map[bwapi.Unit, Tactic] = mutable.Map.empty
  
  override def execute():Iterable[Tactic] = {
    val workers = With.recruiter.getUnits(requirementsOptimal)
    
    _tactics.keySet.diff(workers).foreach(_tactics.remove)
    workers.filterNot(_tactics.contains).foreach(worker => _tactics.put(worker, new TacticGatherMinerals(worker)))
    _tactics.values
  }
}
