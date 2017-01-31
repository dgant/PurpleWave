package Types.Plans

import Startup.With
import Types.Requirements.{RequireUnits, RequireUnitsGreedy}
import Types.Tactics.{Tactic, TacticGatherMinerals}
import UnitMatching.Matcher.UnitMatchWorker

import scala.collection.mutable

class PlanGatherMinerals extends Plan {
  
  override val requirements:RequireUnits = new RequireUnitsGreedy(1, UnitMatchWorker)
  
  val _tactics:mutable.Map[bwapi.Unit, Tactic] = mutable.Map.empty
  
  override def execute():Iterable[Tactic] = {
    val workers = With.recruiter.getUnits(requirements)
    
    _tactics.keySet.diff(workers).foreach(_tactics.remove)
    workers.filterNot(_tactics.contains).foreach(worker => _tactics.put(worker, new TacticGatherMinerals(worker)))
    _tactics.values
  }
}
