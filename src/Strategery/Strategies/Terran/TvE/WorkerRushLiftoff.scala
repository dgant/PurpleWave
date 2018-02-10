package Strategery.Strategies.Terran.TvE

import Planning.Plan
import Planning.Plans.GamePlans.Terran.Standard.TvE.WorkerRushLiftoff
import Strategery.Strategies.Strategy
import bwapi.Race

object WorkerRushLiftoff extends Strategy {
  
  override def gameplan: Option[Plan] = Some(new WorkerRushLiftoff)
  
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
}
