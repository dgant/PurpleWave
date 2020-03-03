package Strategery.Strategies.AllRaces

import Planning.Plan
import Planning.Plans.GamePlans.AllRaces.WorkerRush
import Strategery.Strategies.Strategy
import bwapi.Race

object WorkerRush extends Strategy {
  
  override def gameplan: Option[Plan] = Some(new WorkerRush)
  
  override def ourRaces: Iterable[Race] = Vector(Race.Protoss)
  
  override def minimumGamesVsOpponent: Int = 12
}
