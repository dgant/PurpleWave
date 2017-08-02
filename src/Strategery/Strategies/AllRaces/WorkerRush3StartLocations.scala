package Strategery.Strategies.AllRaces

import Planning.Plan
import Planning.Plans.Protoss.GamePlans.WorkerRush
import Strategery.Strategies.Strategy

object WorkerRush3StartLocations extends Strategy {
  
  override lazy val buildGameplan: Option[Plan] = Some(new WorkerRush)
  
  override def startLocationsMin = 3
}
