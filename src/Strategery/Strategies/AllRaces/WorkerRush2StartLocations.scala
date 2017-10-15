package Strategery.Strategies.AllRaces

import Planning.Plan
import Planning.Plans.Protoss.GamePlans.Specialty.WorkerRush
import Strategery.Strategies.Strategy

object WorkerRush2StartLocations extends Strategy {
  
  override def gameplan(): Option[Plan] = { Some(new WorkerRush) }
  
  override def startLocationsMin = 2
  override def startLocationsMax = 2
}
