package Strategery.Strategies.AllRaces

import Planning.Plan
import Planning.Plans.GamePlans.AllRaces.WorkerRush
import Strategery.Strategies.Strategy

object WorkerRush extends Strategy {
  
  override lazy val gameplan: Option[Plan] = Some(new WorkerRush)
  
  override def minimumGamesVsOpponent: Int = 12
}
