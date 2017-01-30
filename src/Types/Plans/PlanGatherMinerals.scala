package Types.Plans

import Startup.With
import Types.Requirements.{PriorityMinimum, RequireUnits}
import Types.Tactics.{Tactic, TacticGatherMinerals}
import UnitMatching.Matcher.UnitMatchWorker

import scala.collection.JavaConverters._

class PlanGatherMinerals extends Plan {
  
  override val requirementsOptional:RequireUnits = new RequireUnits(this, PriorityMinimum, UnitMatchWorker, Integer.MAX_VALUE)
  
  override def execute():Iterable[Tactic] = {
    return With.game
      .getAllUnits().asScala
      .filter(unit => unit.canGather())
      .map(unit => new TacticGatherMinerals(unit))
  }
}
