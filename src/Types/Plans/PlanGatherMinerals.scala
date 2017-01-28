package Types.Plans

import Startup.With
import Types.Tactics.{Tactic, TacticGatherMinerals}

import scala.collection.JavaConverters._

class PlanGatherMinerals extends Plan {

  override def execute():Iterable[Tactic] = {
    return With.game
      .getAllUnits().asScala
      .filter(unit => unit.canGather())
      .map(unit => new TacticGatherMinerals(unit))
  }
}
